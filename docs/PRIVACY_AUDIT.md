# Privacy Audit

**Audit date:** 2026-08-31  
**Application:** Close By Android  
**Status:** PASS

## Location privacy

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Exact coordinates never shown publicly | PASS | Distance strings only in listing UI |
| No public lat/lng in API responses to UI | PASS | Mappers expose distance, not raw coords |
| No persistent location history server-side | PASS | Location used for session distance calc only |

## Authentication

| Requirement | Status |
|-------------|--------|
| Email OTP only | PASS |
| No SMS OTP | PASS |
| Anonymous browsing allowed | PASS |

## Contact & PII

| Requirement | Status |
|-------------|--------|
| Native dialer / SMS intents only | PASS |
| Provider phone shown per product policy on detail screens | PASS |
| Anonymous session IDs local + request linkage | PASS |

## Offline data

| Data | Storage | Sync |
|------|---------|------|
| Service listings cache | SharedPreferences (device) | Read-only fallback |
| Saved services (anonymous) | SharedPreferences | Queued on sign-in |
| Recently viewed (max 20) | SharedPreferences | Local only |
| Request draft cache | Encrypted prefs / session storage | Not fake-submitted offline |

## Account deletion

Phase 9 `account_deletion_requests` — user-initiated; processed by admin. No immediate hard delete from client.

## Recommendations

- Clear local caches on explicit sign-out if product requires (optional enhancement).
- Document data retention for notifications (90-day client policy in `NotificationCleanupPolicy`).
