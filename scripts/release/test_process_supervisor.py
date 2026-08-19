import os
import pathlib
import subprocess
import sys
import tempfile
import textwrap
import time
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
SUPERVISOR = ROOT / "scripts/ci/run_with_timeout.py"


def _process_exists(pid: int) -> bool:
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    else:
        return True


class ProcessSupervisorTest(unittest.TestCase):
    def test_timeout_reaps_a_detached_grandchild_before_returning(self):
        self.assertTrue(
            SUPERVISOR.is_file(),
            "bounded CI supervisor must exist before detached-child cleanup can be verified",
        )

        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            pid_file = tmp_path / "grandchild.pid"
            fixture = tmp_path / "spawn_detached_grandchild.py"
            fixture.write_text(
                textwrap.dedent(
                    """
                    import pathlib
                    import subprocess
                    import sys
                    import time

                    pid_file = pathlib.Path(sys.argv[1])
                    grandchild = subprocess.Popen(
                        [sys.executable, "-c", "import time; time.sleep(60)"],
                        start_new_session=True,
                    )
                    pid_file.write_text(str(grandchild.pid), encoding="utf-8")
                    time.sleep(60)
                    """
                ),
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    sys.executable,
                    str(SUPERVISOR),
                    "--timeout-seconds",
                    "1",
                    "--kill-after-seconds",
                    "1",
                    "--",
                    sys.executable,
                    str(fixture),
                    str(pid_file),
                ],
                cwd=ROOT,
                check=False,
                capture_output=True,
                text=True,
                timeout=10,
            )

            self.assertEqual(
                124,
                result.returncode,
                msg=f"stdout:\n{result.stdout}\nstderr:\n{result.stderr}",
            )
            self.assertTrue(pid_file.is_file(), "fixture never created detached grandchild")
            grandchild_pid = int(pid_file.read_text(encoding="utf-8"))

            deadline = time.monotonic() + 2
            while _process_exists(grandchild_pid) and time.monotonic() < deadline:
                time.sleep(0.05)

            self.assertFalse(
                _process_exists(grandchild_pid),
                f"detached grandchild {grandchild_pid} survived supervisor timeout",
            )


if __name__ == "__main__":
    unittest.main()
