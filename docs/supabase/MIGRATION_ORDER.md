# Supabase Migration Order

Apply SQL files in this exact order. Each phase depends on prior schemas.

| Order | File | Phase | Depends on |
|-------|------|-------|------------|
| 1 | `schema.sql` | 1 — Core services | — |
| 2 | `schema_phase3.sql` | 3 — Provider management | Phase 1 |
| 3 | `schema_phase4.sql` | 4 — Service requests | Phases 1, 3 |
| 4 | `schema_phase5.sql` | 5 — Trust & safety | Phases 1, 3, 4 |
| 5 | `schema_phase6.sql` | 6 — Notifications | Phase 5 (`is_admin`) |
| 6 | `schema_phase7.sql` | 7 — Advertisements | Phases 1, 3 |
| 7 | `schema_phase8.sql` | 8 — Admin dashboard | Phases 5–7 |
| 8 | `schema_phase9.sql` | 9 — Account / auth | Phase 1 |
| 9 | `schema_phase10.sql` | 10 — Search indexes | Phase 1 |
| 10 | `schema_phase11.sql` | 11 — Saved services | Phase 9 |
| 11 | `schema_phase12.sql` | 12 — Production hardening | All prior |

## Batch 4 (Phases 13–16)

No new destructive migrations required. Batch 4 adds:

- Client-side offline cache (SharedPreferences)
- Notification event completion (uses existing `notifications` table)
- `PushNotificationGateway` abstraction (optional FCM; `device_tokens` from Phase 6)

## Notes

- Do **not** run `DROP` statements against production without backup.
- Verify RLS with Supabase SQL editor using `auth.uid()` test users after each phase.
- Phase 9 and 11 can be applied after Phase 8 if migrating an existing project in one session.
