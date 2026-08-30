# Close by

**Local service discovery & direct rental marketplace for Android.**

> **BASE PROJECT — FEATURE IMPLEMENTATION NOT STARTED**

## Product

Close by connects customers with nearby providers of:

1. Vehicles
2. Labour
3. Equipment

Core philosophy:

> Find Close → Contact Direct → Agree Direct → Get Service → Pay Direct

Everything past discovery — negotiating, calling, agreeing, paying —
happens directly between customer and provider, outside the app. Close
by does not process payments, take a commission, or provide in-app
messaging/calling.

### Explicitly out of scope (by design)

- Online payment, UPI, card payment, wallet, or any payment gateway
- App commission
- In-app chat or in-app calling (native dialer is used instead)
- SMS OTP / mobile OTP for customers
- Forced login to browse services

Provider accounts may later use **Email OTP**. Calling uses the native
Android dialer; SMS uses the native Messages app.

## Technology stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM architecture
- Kotlin Coroutines + StateFlow
- Navigation Compose
- Supabase (Postgrest + Auth client)
- PostgreSQL (via Supabase)

## Architecture

```
UI (Composable)
  ↓
ViewModel
  ↓
UseCase
  ↓
Repository (interface, domain layer)
  ↓
DataSource (implementation, data layer — Supabase/PostgreSQL)
```

No database queries or business logic live inside Composable functions.

## Folder structure

```
android/app/src/main/java/com/closeby/app/
  core/
    navigation/     top-level destinations, NavHost, bottom bar
    location/       location contracts (GeoPoint, LocationProvider)
    permissions/    runtime permission contracts
    phone/          native dialer launcher (ACTION_DIAL)
    sms/            native SMS launcher (ACTION_SENDTO)
    network/        Supabase client provider
    storage/        local key-value storage contract
    ui/
      theme/        Color, Type, Shape, Theme (teal/blue design language)
      components/   reusable Composables (CloseByCard, PrimaryButton, ...)
    utils/

  data/
    model/          Supabase/Postgres wire models (DTOs)
    repository/     repository implementations
    remote/         Supabase data sources
    mapper/         DTO ↔ domain mappers

  domain/
    model/          domain models (Provider, ServiceListing, ...)
    repository/     repository interfaces
    usecase/        use cases

  feature/
    home/ explore/ service/ provider/ request/
    notification/ profile/ review/ report/ verification/
```

Feature folders without a screen file yet (`service`, `provider`,
`review`, `report`, `verification`) hold a `.gitkeep` placeholder —
they're reserved for future feature-specific agent work.

## Navigation foundation

Bottom navigation with five top-level destinations, each currently a
placeholder screen:

- Home
- Explore
- Requests
- Notifications
- Profile

## Design language

Teal + blue palette, soft gradients, white surfaces, rounded cards and
icons, compact layout, clear typography, large touch targets. See
`core/ui/theme/` for tokens and `core/ui/components/` for reusable
building blocks (`CloseByCard`, `GradientSurface`, `PrimaryButton`).

## Supabase configuration

No secrets are committed. Copy `.env.example` values into a local,
git-ignored `local.properties` file at the repo root:

```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

These are read into `BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_ANON_KEY`
at build time. The database schema itself is not implemented yet.

## Development rules

See [`docs/AI_DEVELOPMENT_CONTRACT.md`](docs/AI_DEVELOPMENT_CONTRACT.md)
for the full rules AI coding agents must follow on this repo (scope
discipline, no silent architecture changes, no unnecessary dependencies,
no exposed secrets, must build, must include tests, and more).

## Current project status

**Base project only.** What exists:

- Clean module/folder structure (core / data / domain / feature)
- Navigation foundation (5 placeholder screens, bottom bar, NavHost)
- Theme foundation (color, type, shape, reusable components)
- Supabase client wiring (config placeholders, no schema yet)
- Location / permissions / phone / sms contracts (interfaces only)
- One example domain slice (Provider) showing the intended
  UI → ViewModel → UseCase → Repository → DataSource flow end to end,
  as a pattern for feature agents to follow

**Not implemented yet:** complete Home UI, complete Explore/nearby
search, provider dashboard, admin dashboard, requests, reviews,
advertisements, verification, search, and the nearby-matching
algorithm. These are separate feature tasks.
