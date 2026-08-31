# Close by

**Local service discovery & direct rental marketplace for Android.**

Native Kotlin / Jetpack Compose app backed by Supabase.

## Product

Close by connects customers with nearby providers of vehicles, labour, and equipment.

> Find Close → Contact Direct → Agree Direct → Get Service → Pay Direct

Payments, in-app chat, and in-app calling are out of scope. Native dialer and SMS are used for contact.

## Technology stack

- Kotlin, Jetpack Compose, Material 3
- MVVM + Clean Architecture
- Coroutines, StateFlow, Navigation Compose
- Supabase (Postgrest + Email OTP Auth)

## Architecture

```
UI (Composable) → ViewModel → UseCase → Repository (domain) → DataSource (Supabase / local)
```

## Setup

1. Clone the repository.
2. Copy `.env.example` guidance into `android/local.properties`:
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```
3. Apply Supabase migrations in order — see `docs/supabase/MIGRATION_ORDER.md`.
4. Open `android/` in Android Studio or build from CLI.

## Build commands

```bash
cd android
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Release build requires signing — see `docs/RELEASE_SIGNING.md`.

## Testing

```bash
cd android && ./gradlew test
```

Unit tests cover offline queue, error mapping, notifications, navigation routes, and domain logic.

## Features (Phases 1–16)

- Service discovery, nearby search, filters, pagination
- Provider management, availability, verification
- Service requests (customer + provider flows)
- Trust & safety: reviews, reports, blocks
- In-app notifications with deep links
- Advertisements and admin dashboard
- Email OTP authentication, saved services, recently viewed
- Offline browsing, cached listings, offline saved-service queue
- Production hardening and release documentation

## Security

- Only the Supabase **anon** key ships in the client.
- Never commit service-role keys or keystore passwords.
- See `docs/supabase/SECURITY_AUDIT.md` and `docs/PRIVACY_AUDIT.md`.

## Release

Follow `docs/RELEASE_CHECKLIST.md` before beta or production deployment.
