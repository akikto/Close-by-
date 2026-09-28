# Play Store Readiness Checklist

## App identity

| Item | Status | Notes |
|------|--------|-------|
| App name | Close By | Configure in Play Console |
| Package name | `com.closeby.app` | `applicationId` in `build.gradle.kts` |
| Version | `1.0.0-rc1` (versionCode 2) | Batch 5 release candidate |

## Store listing (prepare in Play Console)

- [ ] **Short description** (80 chars max): Local service discovery — find nearby providers and contact directly.
- [ ] **Full description**: Explain vehicles, labour, equipment discovery; no in-app payments; native call/SMS.
- [x] **App icon**: Official `ic_closeby_logo` used for launcher (adaptive icon) and in-app branding via `CloseByLogo`.
- [ ] **Feature graphic**: 1024×500 — **placeholder required** (design asset not in repo).
- [ ] **Screenshots**: Phone + optional tablet — **capture from release candidate build**.
- [ ] **Privacy policy URL**: **PLACEHOLDER** — host policy before production release.
- [ ] **Support email**: **CONFIGURE** in Play Console.
- [ ] **Developer information**: Legal entity / contact per Play requirements.

## Data Safety (Play Console form)

Declare accurately:

| Data type | Collected | Purpose |
|-----------|-----------|---------|
| Email | Yes (optional sign-in) | Account, OTP auth |
| Approximate location | Yes (when permitted) | Nearby search |
| User IDs | Yes | Supabase auth session |
| Photos | Optional | Service/ad images uploaded by providers |

Not collected: payment info, SMS content, contacts list, precise background location.

## Content rating

- [ ] Complete IARC questionnaire in Play Console.
- [ ] No user-generated chat; contact is off-app (phone/SMS).

## Target audience

- [ ] General audience; providers and customers 18+ recommended for contractual services.

## App access

- [ ] **Anonymous browsing** works without login.
- [ ] **Sign-in**: Email OTP — document test account instructions for reviewers if required.
- [ ] **Admin**: Restricted to `is_admin` users server-side.

## Permissions justification

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` | Nearby service discovery |
| `ACCESS_COARSE_LOCATION` | Fallback location |
| `INTERNET` | Supabase API |
| `ACCESS_NETWORK_STATE` | Offline detection |

No `CALL_PHONE`, `READ_CONTACTS`, or background location.

## Account deletion

- In-app: Profile → Delete Account (submits deletion request).
- Document process in privacy policy.

## Ads declaration

- App displays **provider-submitted advertisements** (admin-approved), not third-party ad SDKs.
- Declare in Play Console accordingly.

## Pre-submission build

- [x] `./gradlew test` PASS
- [x] `./gradlew lint` PASS
- [x] `./gradlew assembleDebug` PASS
- [x] `./gradlew bundleRelease` PASS with a real release keystore; verified with `jarsigner` (see `RELEASE_SIGNING.md`)

Release signing is required for release artifacts; the build does not fall back to the debug key. Play Console setup and listing items above remain owner actions.

## Known placeholders

- Privacy policy URL: **not configured in repo**
- Feature graphic / marketing screenshots: **not in repo**
- Backend config: `android/local.properties` is absent in this workspace, so this AAB uses mock data and demo OTP. Configure a test Supabase project and rebuild before inviting Play testers who need real backend behavior.
- FCM push: optional; in-app notifications work without FCM
