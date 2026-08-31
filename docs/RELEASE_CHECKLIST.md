# Release Checklist

Use before beta or production release.

## Build

- [ ] `./gradlew test` passes
- [ ] `./gradlew lint` passes
- [ ] `./gradlew assembleDebug` succeeds
- [ ] `assembleRelease` only when signing secrets configured in CI/local (not in repo)

## Configuration

- [ ] `SUPABASE_URL` and `SUPABASE_ANON_KEY` set via `local.properties` or CI secrets
- [ ] No service-role key in Android or git history
- [ ] `versionCode` / `versionName` bumped in `android/app/build.gradle.kts`

## Signing

- [ ] Release keystore created and stored outside repository
- [ ] `keystore.properties` or CI secrets configured (passwords never committed)
- [ ] See `docs/RELEASE_SIGNING.md` if present, or Android signing docs

## Database

- [ ] All migrations applied per `docs/supabase/MIGRATION_ORDER.md`
- [ ] RLS verified per `docs/supabase/SECURITY_AUDIT.md`

## Functional smoke tests

- [ ] Anonymous: Home → Explore → Service Details → Call/SMS
- [ ] Auth: Sign in → Save → Request → Notifications
- [ ] Provider: My Services → Requests → Accept/Reject
- [ ] Admin: Dashboard → Users/Providers/Services/Reports/Ads
- [ ] Offline: cached listings visible with banner; request submit blocked offline

## Privacy & security

- [ ] `docs/PRIVACY_AUDIT.md` reviewed
- [ ] No exact coordinates in screenshots or API logs shared publicly

## Optional

- [ ] FCM configured + `PushNotificationGateway` implementation swapped from no-op
- [ ] Play Store listing assets prepared
