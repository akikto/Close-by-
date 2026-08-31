# Close by

**Local service discovery & direct rental marketplace for Android.**

Native Kotlin / Jetpack Compose app backed by Supabase (PostgreSQL + Email OTP Auth).

## Status

Phases 1–16 complete. Batch 5 (Phases 17–20) adds final UX gaps, backend index migration, QA audits, and Play Store release candidate (`1.0.0-rc1`).

## Features

- Anonymous browsing + optional Email OTP sign-in
- Nearby search, filters, pagination, saved services, recently viewed
- Service requests (customer + provider flows)
- Trust & safety: reviews, reports, blocks, verification
- In-app notifications with deep links
- Provider management, availability, advertisements
- Admin dashboard
- Offline browsing with sync queue for saved services

## Setup

1. Clone and open `android/` in Android Studio.
2. Add `android/local.properties`:
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```
3. Apply migrations per `docs/supabase/MIGRATION_ORDER.md` (through `schema_phase17.sql`).
4. Build: `./gradlew assembleDebug`

## Commands

```bash
cd android
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Release signing: `docs/RELEASE_SIGNING.md`

## Documentation

| Doc | Purpose |
|-----|---------|
| `docs/ARCHITECTURE.md` | Package structure and patterns |
| `docs/supabase/SECURITY_AUDIT.md` | RLS and secrets |
| `docs/PRIVACY_AUDIT.md` | Privacy compliance |
| `docs/RELEASE_CHECKLIST.md` | Pre-release steps |
| `docs/PLAY_STORE_CHECKLIST.md` | Play Console preparation |

## Security

- **Anon key only** in the Android client — never service-role.
- Exact coordinates are never shown publicly.
- Native dialer/SMS only — no in-app calling or SMS OTP.
