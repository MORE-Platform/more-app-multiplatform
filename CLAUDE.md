# CLAUDE.md

## Project overview

This is the participant-facing mobile app for the MORE health study platform, built with **Kotlin
Multiplatform Mobile (KMP)**: shared business logic in `shared/`, a native Android UI in `androidApp/`
(Jetpack Compose), and a native iOS UI in `iosApp/` (SwiftUI, Xcode project). The app enrolls
participants into studies, runs scheduled "observations" (sensor/survey data collection), stores data
locally, and syncs with a backend (the `more-studymanager-backend` / `more-data-gateway` services).

Implement everything possible in the shared core, only system depending on the native API or UI are implemented platform specific.
Always check available tools and skills. Do not implement APIs that do not exist. 
Always clean and build gradle to regenerate the API. 
Always test the code afterward and write at least unit tests.
Always build the project for android using gradle and ios using xcodebuild, to see that everything works.

Never commit, never push anything, Manual review is always necessary.

## Build & common commands

- Generate the OpenAPI client (**required before first build, and any time `openapi/*.yaml` changes**):
  `./gradlew :shared:generateOpenApiClasses`
  (This task is also wired as a `dependsOn` for compile/KSP/test tasks, so a plain build will trigger it,
  but running it explicitly avoids stale-generated-code confusion.)
- Full build: `./gradlew build`
- Run shared-module unit tests (all KMP tests live here — there is no separate Android/iOS test suite):
  `./gradlew :shared:testDebugUnitTest` (Android target) or `./gradlew :shared:allTests`
- Run a single test class: `./gradlew :shared:testDebugUnitTest --tests "io.redlink.more.observations.ObservationManagerTest"`
- Android app assemble only: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and build/run the `iosApp` scheme (or `BlendedCare`
  scheme for the white-label variant). The iOS app consumes `shared` as a compiled framework — Gradle
  does not build it; Xcode's build phases invoke the KMP framework build via `embedAndSignAppleFrameworkForXcode`.
- Google services files (`google-services.json` / `GoogleService-Info.plist`) are required to build
  either app. `./setup_google_services.sh` creates them from a base64 `GOOGLE_API_KEY` env var; without
  it you must place the files manually in `androidApp/` and `iosApp/iosApp/` respectively.

## High-level architecture

### Module layout
- `shared/` — KMP module (`commonMain`/`androidMain`/`iosMain`/`commonTest`), package root
  `io.redlink.more`. Contains essentially all business logic: networking, persistence, the
  observation/scheduling engine, view models, and moko-resources based localized strings
  (`SharedRes`, edited via `shared/src/commonMain/moko-resources/{base,de}/strings.xml`).
- `androidApp/` — thin Android shell (`io.redlink.more.app.android`): Activities, Compose screens,
  platform services (WorkManager workers, Health Connect, Firebase, Polar BLE), all delegating to
  `shared`.
- `iosApp/` — thin SwiftUI shell consuming the compiled `shared` framework, mirroring the Android
  package structure 1:1 (e.g. `iosApp/iosApp/Observations/` ↔ `androidApp/.../observations/`,
  `Views/PC/` ↔ Android's `pc/composables/`). When adding a feature, expect to touch both native shells
  plus shared code, since almost nothing is platform-shared UI.
- Cross-cutting rule: **when you change a Room entity/schema, bump `version` in
  `shared/src/commonMain/kotlin/io/redlink/more/database/AppDatabase.kt` and add a `Migration` in
  `shared/src/commonMain/kotlin/io/redlink/more/database/migrations/`.** Skipping this crashes the app
  on upgrade for already-installed users. (The README's mention of a "RealmDatabase.kt" is stale — the
  project now uses **Room**, not Realm.)

### Composition root — no DI framework
There's no Koin/Dagger/Hilt graph despite a `koin-bom` dependency in `androidApp/build.gradle.kts`.
Instead, `shared/src/commonMain/kotlin/io/redlink/more/Shared.kt` is a single facade class that
constructs and wires every singleton service (`NetworkService`, `MainRepository`, `ObservationFactory`,
`NotificationManager`, `BluetoothController`, `ObservationDataManager`, …) via constructor injection.
Each platform builds one `Shared` instance at startup by passing in its platform-specific
implementations:
- Android: `MoreApplication.initShared()` (`androidApp/.../MoreApplication.kt`) builds the Room database,
  `AndroidObservationFactory`, `AndroidPollingTaskScheduler`, `AndroidDataRecorder`, etc., and stores the
  singleton `Shared` instance on the `MoreApplication` companion object.
- iOS: the equivalent wiring happens in `iosApp/iosApp/AppDelegate.swift`, constructing
  `IOSObservationFactory`, `IOSObservationPermissionObserver`, `IOSPollingTaskScheduler`, etc.

`expect`/`actual` is reserved for small platform primitives (`Platform.kt`, `DatabaseManager.kt` for the
Room builder, `UUID.kt`, `HttpClientReceiver.kt` for the Ktor engine) — the bigger platform seams
(observation factories, permission observers, polling schedulers, BLE/HealthKit/HealthConnect
collectors) are plain abstract classes in `commonMain` with a concrete subclass per platform
(`AndroidXxx` in Kotlin, `IOSXxx` in Swift interop'ing with the shared framework).

### The observation engine (core domain concept)
"Observations" are the pluggable data-collection units (accelerometer, Health Connect/HealthKit
vitals, app usage, Garmin, Polar heart rate BLE, Limesurvey, in-app questions, ...). Key types in
`shared/src/commonMain/kotlin/io/redlink/more/observations/`:
- `Observation` (`Observation.kt`) — abstract base every observation type extends. Owns the
  start/stop/permission/data-storage lifecycle shared by all types: `start()`/`stop()`,
  permission request/approval via `ObservationPermissionObserver`, writing collected data through
  `storeData`/`storeInstant`/long-running-observation helpers into the `ObservationDataManager`, and
  activating/deactivating background polling (`PollingObservationRegistry`) for types that need it.
- `ObservationFactory` (`ObservationFactory.kt`) — abstract registry that (a) registers built-in
  observation providers (`registerObservation { SomeObservation(repo) }`), (b) lazily instantiates only
  the observation types actually needed by the current study's schedule config
  (`initializeNeededObservations`, following `dependentObservationTypes` transitively), and (c) fans out
  cross-cutting operations (permission checks, error checks, BLE device discovery) across all active
  observations. Platform subclasses (`AndroidObservationFactory`, `IOSObservationFactory`) register the
  platform-specific observation types (Health Connect, HealthKit, Polar, accelerometer) on top of the
  types registered in the shared base class.
- `observationTypes/ObservationType` and its per-type subclasses (`AccelerometerType`, `GPSType`,
  `GarminType`, `QuestionType`, `LimeSurveyType`, `AppUsageObservationType`, health-connect types, …) —
  identify/match an observation by its backend-configured type string and declare required sensor
  permissions and dependent types.
- `Collector`/`PermissionCollector`/`BundledPermissionCollector` (in `ObservationFactory.kt`) and
  `ManualObserver`/`ManualDataCollection` (`observers/`) — smaller interfaces an `Observation`
  implementation composes to request permissions or expose a "collect once, on demand" hook used by
  background polling (`ObservationFactory.pollActiveObservations()`).
- `polling/PollingTaskScheduler` + `PollingObservationRegistry` — cross-platform abstraction over
  background execution (WorkManager `PollingWorker` on Android, `BGAppRefreshTask` via
  `IOSPollingTaskScheduler`/`PollingBackgroundTask` on iOS) used by observations that declare a
  `pollIntervalMillis()`.

To add a new observation type: create an `ObservationType` subclass, an `Observation` subclass in
`shared/commonMain`, register it via `registerObservation` (in the shared `ObservationFactory` if
cross-platform, or in `AndroidObservationFactory`/`IOSObservationFactory` if platform-specific), and add
any needed platform collector implementations under `androidApp/.../observations/` and
`iosApp/iosApp/Observations/`.

### Networking
Ktor-based (`shared/src/commonMain/kotlin/io/redlink/more/services/network/`). `NetworkClients.kt`
lazily builds/caches per-endpoint API clients (`ConfigurationApi`, `DataApi`, `RegistrationApi`,
`NotificationsApi`, `GarminRegistrationApi`) using HTTP Basic Auth from `CredentialRepository`, and
invalidates all cached clients when credentials or the endpoint change. The API classes themselves
(`io.redlink.more.services.network.openapi.*`) are **generated code** from
`openapi/MobileAppAPI.yaml` via the `generateOpenApiClasses` Gradle task — do not hand-edit anything
under `shared/build/generated/open_api/`; change the YAML spec instead and regenerate.
`NetworkServiceProxy` wraps the real `NetworkServiceImpl` with an optional `DemoNetworkService`
(`services/network/demo/`) used for demo-mode/offline walkthroughs.

### Persistence
Room (KMP, via `androidx.room` + KSP) in `shared/src/commonMain/kotlin/io/redlink/more/database/`:
`AppDatabase.kt` declares entities/DAOs; `repository/` wraps DAOs in repository interfaces + impls
(`MainRepository` aggregates all of them and is what most of the rest of the app depends on);
`migrations/` holds `Migration_x_y` classes. Schema JSON snapshots are exported to `shared/schemas/`
(configured via `room { schemaDirectory(...) }`).

### Tests
All automated tests live in `shared/src/commonTest/` (common KMP tests, run on the JVM) — there is no
separate Android instrumented test suite or iOS test target in active use. Tests use hand-written
fakes/mocks under `shared/src/commonTest/kotlin/io/redlink/more/mocks/` (no mocking framework) rather
than a DI container, since the composition root (`Shared`) is wired manually.

## CI/CD
GitHub Actions (`.github/workflows/`) builds both apps via **fastlane** on every push/PR
(`build.yml`) and deploys to TestFlight/Play Store Beta when a `x.x.x` semver tag is pushed
(`deploy-beta.yml`). Android version code for release builds is derived from the semver tag
(`major*10⁴ + minor*10² + patch`), taking the max against the latest Play Store version code. See the
README's "CI/CD Pipeline" section for the full list of required secrets/vars if you need to touch the
fastlane lanes (`androidApp/fastlane/`, `iosApp/fastlane/`).

## Notes
- `graphify-out/` contains generated codebase-graph reports (not hand-maintained; ignore unless asked
  to work with them).
- This repository is frequently worked on via long-lived feature/merge branches (see current branch
  `healthConnectMerge2008`) — check `git status` before assuming the working tree is in a clean,
  fully-compiling state; mid-merge branches can have partially-integrated code.
