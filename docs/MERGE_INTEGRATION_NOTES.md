# Merge integration notes — five agent deliverables → one app

This project merges five separately-built deliverables into the base
project's single `:app` module:

| Source zip | Package(s) | Owns |
|---|---|---|
| `Close-by-BASE-v1.zip` | `com.closeby.app.*` | Scaffold, navigation, theme, Supabase client, Provider slice |
| `Close-by-BASE-v1_zipClose-by-LOCATION-v1.zip` | `com.closeby.feature.nearby.*` | Device location, distance calc, radius search |
| `Close-by-PROVIDER-v1.zip` / `Close-by-SERVICE-SEARCH-v1.zip` (identical content) | `com.closeby.feature.servicelisting.*` | Search / filter / sort / listing + details UI |
| `closeby-agent5-task05.zip` | `com.closeby.contact.*`, `com.closeby.request.*`, `com.closeby.availability.*`, `com.closeby.util.*` | Call/SMS launch, service requests, provider availability |

All five keep their own package namespace (no renaming was needed — Kotlin
packages don't have to match the app's `com.closeby.app` application ID),
so nothing from the original modules had to be rewritten.

## What was done to make it build and run

1. **Copied all source + test files** into `app/src/main/java/com/closeby/...`
   and `app/src/test/java/com/closeby/...`, matching each file's own
   package declaration.
2. **Deduplicated** `Close-by-PROVIDER-v1.zip` and
   `Close-by-SERVICE-SEARCH-v1.zip` — they were byte-for-byte identical, so
   only one copy was kept.
3. **`build.gradle.kts`** — added the dependencies the merged modules need
   that the base project didn't already declare:
   - `io.coil-kt:coil-compose:2.6.0` (image loading in the service listing UI)
   - `org.robolectric:robolectric`, `androidx.test:core` (only used by
     `AndroidContactLauncherTest`)
   - `kotlinx-coroutines-test`, `app.cash.turbine:turbine` (test-only)
   - Core library desugaring (`coreLibraryDesugaring`,
     `isCoreLibraryDesugaringEnabled = true`) — the request/availability
     features use `java.time.LocalDate`/`LocalTime`, which needs
     desugaring to run on `minSdk 24`.
   - `testOptions.unitTests` config required by Robolectric.
4. **`AndroidManifest.xml`** — no change needed; the base project had
   already pre-declared the location permissions the LOCATION module asks
   for.
5. **Navigation wiring** — `Explore` and `Requests` (previously
   "coming soon" placeholders) now render the real feature screens:
   - `Explore` → `ServiceListingScreen` (search/filter/sort), backed by
     the servicelisting module's own `MockServiceRepository` /
     `MockLocationProvider` (ships with the module for previews).
   - `Requests` → `CustomerRequestsScreen`, backed by a new
     `InMemoryServiceRequestRepository` (see below) so the tab has data
     to show immediately.

## Still stubbed / not wired — do this next

- **`InMemoryServiceRequestRepository`** (new, at
  `request/data/mock/`) is in-memory only and does **not** enforce
  authorization. Replace it with a Supabase-backed implementation before
  shipping — Agent 5's own notes flag this as required.
- **`MockServiceRepository` / `MockLocationProvider`** (shipped inside
  `feature/servicelisting/data/mock/MockServiceDataSource.kt`) return
  fixed sample listings and do no real distance math. Replace with a
  Supabase-backed `ServiceRepository` and an adapter over
  `com.closeby.feature.nearby`'s `GetNearbyServicesUseCase` /
  `AndroidLocationProvider`.
- **`com.closeby.feature.nearby`** (location module) is merged and
  unit-tested but not yet wired into any screen — it's available for the
  Explore screen's radius search once the servicelisting `LocationProvider`
  adapter above is built.
- **Service Details, Provider Requests, Create Request, Availability**
  screens all exist in source but aren't reachable from
  `CloseByNavHost` yet — they need routes/args (e.g. `serviceId`,
  `providerId`) added.
- **`ContactButtons`** (call/SMS) isn't dropped into any screen yet — it's
  meant for the Service Details / provider screen per Agent 5's notes.
- No dependency injection framework is used — ViewModels are built with
  small manual `ViewModelProvider.Factory` classes right in the screen
  files (`ExploreScreen.kt`, `RequestsScreen.kt`). Fine for now; swap for
  Hilt/Koin if the project adopts one later.

## Not verified

No Android SDK/Gradle toolchain was available in the sandbox that
produced or merged these modules, so **nothing here has been compiled**.
Please run `./gradlew :app:assembleDebug` and
`./gradlew :app:testDebugUnitTest` after pulling this in, and fix any
import/version mismatches Gradle surfaces.
