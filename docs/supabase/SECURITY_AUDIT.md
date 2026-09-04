# Supabase Security Audit

**Audit date:** 2026-09-04  
**Scope:** Phases 1–18 migrations + production notification hardening  
**Status:** PASS with documented follow-ups

## Client credentials

| Check | Status | Notes |
|-------|--------|-------|
| No service-role key in Android source | PASS | Only `SUPABASE_URL` + `SUPABASE_ANON_KEY` via `BuildConfig` |
| No hard-coded passwords/tokens | PASS | Repository scan found no embedded secrets |
| Public anon key only in client | PASS | `local.properties` / CI secrets for build-time injection |
| Release keystore not committed | PASS | `keystore.properties` / `*.keystore` git-ignored; CI uses secrets |

## Row Level Security (RLS)

| Table / area | RLS enabled | Policy summary |
|--------------|-------------|----------------|
| `services`, `providers` | Yes | Public read for active listings; provider ownership on write |
| `service_requests` | Yes | Customer/provider ownership; anonymous session rules |
| `notifications` | Yes | User reads/updates own rows; **insert self only** (Phase 18) |
| `device_tokens` | Yes | User manages own tokens only |
| `saved_services` (Phase 11) | Yes | User owns saved rows |
| `reviews`, `reports`, `blocks` (Phase 5) | Yes | Scoped to participants / reporters |
| `advertisements` (Phase 7) | Yes | Owner + admin moderation |
| `admin_*` views (Phase 8) | Yes | `is_admin()` gate |
| `account_deletion_requests` (Phase 9) | Yes | User submits own; admin reads/updates |

## Phase 18 — Server-side notifications

| Check | Status | Notes |
|-------|--------|-------|
| `create_notification_for_user()` SECURITY DEFINER | PASS | `REVOKE` from `public`, `authenticated`, `anon` |
| Cross-user events via DB triggers | PASS | Requests, reviews, verification, ads, reports, deletion status |
| Client cross-user notification insert | BLOCKED | RLS `notifications_insert` = `user_id = auth.uid()` only |
| Android client self-notifications only (Supabase mode) | PASS | `NotificationEventHandler.canPersistClientNotification()` |
| Admin/review/ad/report notifications from client | REMOVED | Triggers handle when Supabase configured |

## Findings

1. **Notifications** — Phase 18 removes admin insert on `notifications`. Cross-user delivery is trigger-only; clients may insert only for `auth.uid()`.
2. **Anonymous request cache** — Local-only cache in `AndroidClientSessionStorage` is not synced to Supabase without auth. RLS boundaries preserved.
3. **FCM** — `device_tokens` table exists; Android uses `NoOpPushNotificationGateway` until FCM credentials are configured.
4. **Account deletion** — Users submit requests; only admins update status via `account_deletion_admin_update` policy.

## Recommendations

- Apply `schema_phase18.sql` in production after backup (see `MIGRATION_ORDER.md`).
- Rotate anon key if ever committed accidentally.
- Enable Supabase Auth email OTP only (no SMS OTP in app).
- Review `is_admin()` function after each schema change.

## Android security boundaries

- ViewModels do not execute raw SQL.
- Exact coordinates are never shown in UI mappers.
- Service requests cannot be marked submitted without server `create` success when online.
- Admin deletion queue requires `is_admin()` on server and client gate.
