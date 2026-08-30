# AI Development Contract — Close by

This document governs how AI coding agents (including Claude) work on this
codebase. Every agent working on a feature branch must follow these rules.

1. **Scope discipline.** AI agents must work only on the feature(s) they
   were explicitly assigned. Do not pick up adjacent work "while you're
   in there."

2. **No unrelated rewrites.** AI agents must not rewrite modules outside
   their assigned scope, even if the code looks improvable. Flag it
   instead of changing it.

3. **Architecture is fixed.** AI agents must not change the
   UI → ViewModel → UseCase → Repository → DataSource architecture, the
   MVVM pattern, or the top-level folder structure without explicit
   permission from a maintainer.

4. **No unnecessary dependencies.** Do not add a library to solve a
   problem that existing dependencies (Compose, Coroutines, Navigation
   Compose, Supabase-kt) already solve. Propose new dependencies
   separately before adding them.

5. **No exposed secrets.** Never hardcode Supabase URLs, API keys, or any
   credential in source. Configuration comes from `local.properties`
   (git-ignored) per `.env.example`. Never print secrets in logs, commit
   messages, or generated docs.

6. **Report changed files.** Every change must come with an explicit list
   of files created and files modified. No silent edits.

7. **Must build.** Every feature branch must compile successfully before
   being handed off. A broken build is not an acceptable deliverable.

8. **Must include tests.** Every feature must include appropriate unit
   and/or instrumentation tests for the logic it adds — at minimum for
   ViewModel and UseCase layers.

9. **Don't remove working functionality.** Existing functionality must
   not be deleted or disabled without an explicit, stated reason.

10. **Don't change contracts silently.** Database schemas, Supabase table
    shapes, and repository/use-case interfaces are contracts shared
    across features. Changing them requires explicit call-out, not a
    silent diff.

## Non-negotiable product rules (apply to all future feature work)

- No online payment, UPI, card payment, wallet, or in-app payment gateway.
- No app commission logic.
- No in-app chat or in-app calling — calling uses the native Android
  dialer (`ACTION_DIAL`), SMS uses the native Messages app
  (`ACTION_SENDTO`).
- No SMS OTP or mobile OTP for customers. Browsing services never
  requires login. Provider accounts may later use **Email** OTP only.

## Reporting format

At the end of any task, an agent should report:

```
Completed
...
Files Created
...
Files Modified
...
Dependencies Added
...
Build Result: PASS / FAIL
Tests
...
Known Issues
...
Next Recommended Feature
...
```
