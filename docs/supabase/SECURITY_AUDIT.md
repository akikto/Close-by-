# Supabase Security Audit

**Audit date:** 2026-08-31  
**Scope:** Phases 1–12 migrations + Batch 4 client changes  
**Status:** PASS with documented follow-ups

## Client credentials

| Check | Status | Notes |
|-------|--------|-------|
| No service-role key in Android source | PASS | Only `SUPABASE_URL` + `SUPABASE_ANON_KEY` via `BuildConfig` |
| No hard-coded passwords/tokens | PASS | Repository scan found no embedded secrets |
| Public anon key only in client | PASS | `local.properties` / CI secrets for build-time injection |

## Row Level Security (RLS)

| Table / area | RLS enabled | Policy summary |
|--------------|-------------|----------------|
| `services`, `providers` | Yes | Public read for active listings; provider ownership on write |
| `service_requests` | Yes | Customer/provider ownership; anonymous session rules |
| `notifications` | Yes | User reads/updates own rows; admin insert |
| `device_tokens` | Yes | User manages own tokens only |
| `saved_services` (Phase 11) | Yes | User owns saved rows |
| `reviews`, `reports`, `blocks` (Phase 5) | Yes | Scoped to participants / reporters |
| `advertisements` (Phase 7) | Yes | Owner + admin moderation |
| `admin_*` views (Phase 8) | Yes | `is_admin()` gate |
| `account_deletion_requests` (Phase 9) | Yes | User submits own; admin processes |

## Batch 5 additions (Phase 17–20)

| Area | Status |
|------|--------|
| Saved-service favorite toggle on cards/details | Implemented |
| Anonymous → account migration prompt | Implemented |
| Blocked providers management screen | Implemented |
| Notification publishers (trust/admin/ads/account) | Wired via `NotificationEventPublisher` |
| `schema_phase17.sql` index migration | Created — **NOT executed** in agent environment |
| Version `1.0.0-rc1` (versionCode 2) | Release candidate |

## Findings

1. **Admin notification insert** — `notifications_insert` allows `is_admin()` inserts. Correct for server-side/admin flows; Android never uses service role.
2. **Anonymous request cache** — Local-only cache in `AndroidClientSessionStorage` is not synced to Supabase without auth. RLS boundaries preserved.
3. **FCM** — `device_tokens` table exists; Android uses `NoOpPushNotificationGateway` until FCM credentials are configured.

## Recommendations

- Rotate anon key if ever committed accidentally.
- Apply migrations in order documented in `MIGRATION_ORDER.md`.
- Enable Supabase Auth email OTP only (no SMS OTP in app).
- Review `is_admin()` function after each schema change.

## Android security boundaries

- ViewModels do not execute raw SQL.
- Exact coordinates are never shown in UI mappers.
- Service requests cannot be marked submitted without server `create` success when online.
