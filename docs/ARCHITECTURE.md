# Close By — Architecture

## Overview

Native Android (Kotlin + Jetpack Compose) local service discovery app backed by Supabase (PostgreSQL + Auth).

```
Compose UI → ViewModel → UseCase (where present) → Repository (domain) → DataSource / Local cache
```

## Modules (logical packages)

| Package | Responsibility |
|---------|----------------|
| `core/` | Navigation, DI factories, network monitor, session, theme |
| `feature/` | Route composables wiring screens to ViewModels |
| `feature/servicelisting/` | Discovery, filters, saved services, service details |
| `feature/nearby/` | Location permission + nearby host |
| `feature/provider/` | Provider management, auth |
| `request/` | Service requests lifecycle |
| `trust/` | Reviews, reports, blocks, verification |
| `notification/` | In-app notifications + event bridge |
| `admin/` | Admin dashboard |
| `advertisement/` | Ad campaigns |
| `data/` | Supabase DTOs, mappers, repository implementations |

## Key patterns

- **DI:** `*DependenciesFactory` objects (no Hilt) — single composition root per module.
- **Offline:** `OfflineAwareServiceRepository` (listings), `OfflineAwareSavedServiceRepository` (favorites).
- **Notifications:** `RequestNotificationBridge` + `AppNotificationEventBridge` → `NotificationEventHandler` → Supabase.
- **Push:** `PushNotificationGateway` abstraction; default `NoOpPushNotificationGateway`.
- **Auth:** Email OTP only; anonymous browsing without sign-in.
- **Contact:** Native `ACTION_DIAL` / `ACTION_SENDTO` — no in-app calling.

## Security boundaries

- Android client uses **anon key only**.
- RLS enforced in Supabase; admin via `is_admin()`.
- Exact coordinates never exposed in public UI.
- Service-role key must never ship in the app.

## Navigation

Bottom tabs: Home, Explore, Requests, Notifications, Profile.

Deep links from notifications route to request details, verification (profile), advertisements (my ads), admin dashboard.

## Testing

Unit tests under `android/app/src/test/` — domain validators, repositories (mock), ViewModels, navigation routes.

CI: `./gradlew test lint assembleDebug` via GitHub Actions.
