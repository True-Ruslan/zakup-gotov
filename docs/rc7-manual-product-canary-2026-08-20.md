# v0.1.0-rc.7 manual product canary

Status: **EVIDENCE HARNESS IN DEVELOPMENT / MANUAL VERDICT PENDING**

Immutable release source:

```text
v0.1.0-rc.7
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

This canary must exercise the published digest-pinned `rc.7` release bundle. It must not rebuild application images, move tags, mutate OCI `latest`, or treat automation as manual acceptance.

## Review scenarios

1. WeeklyPlan → Pantry → comparison → optimization.
2. Private local draft save → reload → restore → explicit clear.
3. Recipe comparison.
4. Manual-list comparison.
5. Desktop and narrow/mobile layout sanity.
6. API unavailable state, API recovery, and reload/restart sanity.

The harness may automate environment startup and evidence capture. The final product verdict remains a separate manual review of screenshots/report and any observed defects.

## Acceptance boundary

Stable `v0.1.0` remains blocked until issue #152 records a satisfactory manual review against the immutable release evidence. A green evidence-capture workflow by itself is **not** an acceptance verdict.
