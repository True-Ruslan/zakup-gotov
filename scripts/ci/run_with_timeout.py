#!/usr/bin/env python3
from __future__ import annotations

import argparse
import ctypes
import dataclasses
import os
import pathlib
import shutil
import signal
import subprocess
import sys
import time


EXIT_TIMED_OUT = 124
EXIT_INTERNAL_ERROR = 125
PR_SET_CHILD_SUBREAPER = 36
PROC_ROOT = pathlib.Path("/proc")


@dataclasses.dataclass(frozen=True)
class ProcessIdentity:
    pid: int
    ppid: int
    start_time: int
    state: str


def _read_process(pid: int) -> ProcessIdentity | None:
    try:
        stat = (PROC_ROOT / str(pid) / "stat").read_text(encoding="utf-8")
    except (FileNotFoundError, PermissionError, ProcessLookupError):
        return None

    comm_end = stat.rfind(")")
    if comm_end < 0:
        return None

    fields = stat[comm_end + 2 :].split()
    if len(fields) < 20:
        return None

    return ProcessIdentity(
        pid=pid,
        state=fields[0],
        ppid=int(fields[1]),
        start_time=int(fields[19]),
    )


def _snapshot_processes() -> dict[int, ProcessIdentity]:
    processes: dict[int, ProcessIdentity] = {}
    if not PROC_ROOT.is_dir():
        return processes

    for entry in PROC_ROOT.iterdir():
        if not entry.name.isdigit():
            continue
        identity = _read_process(int(entry.name))
        if identity is not None:
            processes[identity.pid] = identity
    return processes


def _descendants(root_pid: int) -> list[tuple[int, ProcessIdentity]]:
    processes = _snapshot_processes()
    by_parent: dict[int, list[ProcessIdentity]] = {}
    for identity in processes.values():
        by_parent.setdefault(identity.ppid, []).append(identity)

    result: list[tuple[int, ProcessIdentity]] = []
    pending: list[tuple[int, int]] = [(root_pid, 0)]
    while pending:
        parent_pid, depth = pending.pop()
        for child in by_parent.get(parent_pid, []):
            child_depth = depth + 1
            result.append((child_depth, child))
            pending.append((child.pid, child_depth))
    return result


def _same_live_process(identity: ProcessIdentity) -> bool:
    current = _read_process(identity.pid)
    return (
        current is not None
        and current.start_time == identity.start_time
        and current.state != "Z"
    )


def _enable_subreaper() -> None:
    if not sys.platform.startswith("linux"):
        return

    libc = ctypes.CDLL(None, use_errno=True)
    if libc.prctl(PR_SET_CHILD_SUBREAPER, 1, 0, 0, 0) != 0:
        errno = ctypes.get_errno()
        raise OSError(errno, "prctl(PR_SET_CHILD_SUBREAPER) failed")


def _signal_identity(identity: ProcessIdentity, sig: signal.Signals) -> None:
    if not _same_live_process(identity):
        return

    try:
        os.kill(identity.pid, sig)
        return
    except ProcessLookupError:
        return
    except PermissionError:
        pass

    sudo = shutil.which("sudo")
    kill = shutil.which("kill")
    if sudo is None or kill is None:
        raise RuntimeError(
            f"cannot signal privileged descendant pid={identity.pid}: sudo/kill unavailable"
        )

    if not _same_live_process(identity):
        return

    completed = subprocess.run(
        [sudo, "-n", kill, f"-{int(sig)}", "--", str(identity.pid)],
        check=False,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
        text=True,
        timeout=5,
    )
    if completed.returncode != 0 and _same_live_process(identity):
        raise RuntimeError(
            f"failed to signal privileged descendant pid={identity.pid}: "
            f"{completed.stderr.strip()}"
        )


def _signal_process_group(pid: int, sig: signal.Signals) -> None:
    try:
        os.killpg(pid, sig)
    except ProcessLookupError:
        return
    except PermissionError as exc:
        raise RuntimeError(f"cannot signal process group {pid}: {exc}") from exc


def _reap_adopted_children() -> None:
    if not sys.platform.startswith("linux"):
        return

    while True:
        try:
            pid, _ = os.waitpid(-1, os.WNOHANG)
        except ChildProcessError:
            return
        if pid == 0:
            return


def _live_identities(identities: dict[tuple[int, int], ProcessIdentity]) -> list[ProcessIdentity]:
    return [identity for identity in identities.values() if _same_live_process(identity)]


def _remember(
    identities: dict[tuple[int, int], ProcessIdentity],
    items: list[tuple[int, ProcessIdentity]],
) -> None:
    for _, identity in items:
        identities[(identity.pid, identity.start_time)] = identity


def _terminate_tree(process: subprocess.Popen[object], kill_after_seconds: float) -> None:
    tracked: dict[tuple[int, int], ProcessIdentity] = {}
    root_identity = _read_process(process.pid)
    if root_identity is not None:
        tracked[(root_identity.pid, root_identity.start_time)] = root_identity
    _remember(tracked, _descendants(process.pid))

    descendants = sorted(
        _descendants(process.pid),
        key=lambda item: item[0],
        reverse=True,
    )
    for _, identity in descendants:
        _signal_identity(identity, signal.SIGTERM)
    _signal_process_group(process.pid, signal.SIGTERM)

    deadline = time.monotonic() + kill_after_seconds
    while time.monotonic() < deadline:
        _remember(tracked, _descendants(os.getpid()))
        if not _live_identities(tracked):
            break
        time.sleep(0.05)

    _remember(tracked, _descendants(os.getpid()))
    survivors = _live_identities(tracked)
    for identity in survivors:
        _signal_identity(identity, signal.SIGKILL)

    try:
        process.wait(timeout=max(1.0, min(kill_after_seconds, 5.0)))
    except subprocess.TimeoutExpired:
        _signal_process_group(process.pid, signal.SIGKILL)
        process.wait(timeout=5)

    reap_deadline = time.monotonic() + max(1.0, min(kill_after_seconds, 5.0))
    while time.monotonic() < reap_deadline:
        _reap_adopted_children()
        _remember(tracked, _descendants(os.getpid()))
        if not _live_identities(tracked):
            break
        for identity in _live_identities(tracked):
            _signal_identity(identity, signal.SIGKILL)
        time.sleep(0.05)

    _reap_adopted_children()
    survivors = _live_identities(tracked)
    if survivors:
        pids = ", ".join(str(identity.pid) for identity in survivors)
        raise RuntimeError(f"timed-out command left live descendant process(es): {pids}")


def _shell_status(returncode: int) -> int:
    return 128 + (-returncode) if returncode < 0 else returncode


def _parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run one CI command with bounded, descendant-safe timeout cleanup."
    )
    parser.add_argument("--timeout-seconds", type=float, required=True)
    parser.add_argument("--kill-after-seconds", type=float, default=15.0)
    parser.add_argument("command", nargs=argparse.REMAINDER)
    args = parser.parse_args()

    if args.command and args.command[0] == "--":
        args.command = args.command[1:]
    if not args.command:
        parser.error("a command is required after --")
    if args.timeout_seconds <= 0:
        parser.error("--timeout-seconds must be greater than zero")
    if args.kill_after_seconds <= 0:
        parser.error("--kill-after-seconds must be greater than zero")
    return args


def main() -> int:
    args = _parse_args()

    if os.name != "posix":
        print("run_with_timeout.py requires a POSIX runner", file=sys.stderr)
        return EXIT_INTERNAL_ERROR

    try:
        _enable_subreaper()
        process = subprocess.Popen(args.command, start_new_session=True)
    except FileNotFoundError as exc:
        print(f"failed to invoke command: {exc}", file=sys.stderr)
        return 127
    except Exception as exc:
        print(f"failed to start bounded command: {exc}", file=sys.stderr)
        return EXIT_INTERNAL_ERROR

    try:
        return _shell_status(process.wait(timeout=args.timeout_seconds))
    except subprocess.TimeoutExpired:
        print(
            f"command timed out after {args.timeout_seconds:g}s; terminating its process tree",
            file=sys.stderr,
        )

    try:
        _terminate_tree(process, args.kill_after_seconds)
    except Exception as exc:
        print(f"failed to clean timed-out command tree: {exc}", file=sys.stderr)
        return EXIT_INTERNAL_ERROR

    return EXIT_TIMED_OUT


if __name__ == "__main__":
    raise SystemExit(main())
