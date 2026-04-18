# TeamFlow Manager

> ⚽ App de gestión de equipos de fútbol infantil. Controla minutos, cambios, asistencias y estadísticas en tiempo real. La herramienta esencial para el entrenador.

[![CI](https://github.com/jesuslcorominas/teamflowmanager/actions/workflows/pr-checks.yml/badge.svg?branch=develop)](https://github.com/jesuslcorominas/teamflowmanager/actions/workflows/pr-checks.yml)
[![Version](https://img.shields.io/badge/version-0.5.0--alpha-orange)](https://github.com/jesuslcorominas/teamflowmanager/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin)
![KMP](https://img.shields.io/badge/KMP-Android%20%7C%20iOS-blueviolet?logo=kotlin)
![Android](https://img.shields.io/badge/Min%20SDK-29-green?logo=android)

---

## Features

### Live match tracking
- **Match clock** — Global stopwatch with pause, resume and timeout support
- **Player timers** — Individual per-player stopwatch accumulating real on-field time
- **Goals** — Record goals with scorer, own goals and opponent goals
- **Substitutions** — Log player in/out substitutions with automatic time tracking
- **Periods** — Half-time format (2 × 25 min) and quarter-time format (4 × 12:30), covering 5-a-side, 7-a-side, 8-a-side and 11-a-side

### Squad & lineup management
- **Match wizard** — Step-by-step squad call-up, starting lineup selection and captain assignment
- **Player positions** — 12 positions from goalkeeper to striker
- **Captain tracking** — Default captain, current captain and historical captain log
- **Substitution bench** — Available substitutes with real-time status

### Club & team organisation
- **Clubs** — Create a club or join one via invitation code
- **Multiple teams** — Organise several teams under the same club
- **Role-based access** — President and Coach roles with different views and permissions
  - *President*: manage members, assign coaches, view all teams, receive notifications
  - *Coach*: manage their team, players and matches
- **Coach assignment** — Presidents assign coaches; users can also self-assign to a team
- **Member management** — Invite, view and expel club members

### Statistics & reports
- **Match timeline** — Goals, substitutions, timeouts and period breaks in chronological order
- **Score evolution** — Goal progression chart across the match
- **Player activity** — Individual on-field time chart per match
- **Player stats** — Accumulated minutes played and goals across all matches
- **PDF export** — Full match report and player statistics as a PDF document

### Push notifications
- **President notifications** — Real-time alerts for coach assignments and member join requests (Firebase Cloud Messaging)
- **Topic subscriptions** — Automatically subscribed to club-wide notifications on join
- **Match event notifications** — Presidents receive push notifications for match start, end, and goals scored by any team in their club
  - Content: scoring team name, current scoreline, and minute of play (overtime shown as `XX+Y`, e.g. `25+2`)
  - Per-match collapsing: only the latest event per match appears in the status bar (FCM `tag` + APNS `apns-collapse-id`)
  - In-app notification list in the president's club section — one entry per match, updated on each event, reset to unread on new events
- **Notification preferences** — Global toggles for match events and goals, with per-team overrides, configurable from Settings

### Invitations & deep linking
- **Team invitations** — Shareable invitation links with regenerable codes (Firebase Cloud Functions short links)
- **Deep links** — Direct app navigation from notifications and shared links (`teamflowmanager://`)

### Cross-platform & sync
- **Kotlin Multiplatform** — Android and iOS apps sharing ~90% of their code
- **Google Sign-In** — Authentication on Android (Credential Manager) and iOS (GIDSignIn)
- **Firestore sync** — Real-time data synchronisation across devices

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin 2.1.0 (KMP) |
| **UI — Android** | Jetpack Compose + Material 3 |
| **UI — iOS** | Compose Multiplatform 1.7.3 (CMP) |
| **Architecture** | Clean Architecture + MVVM |
| **DI** | Koin 4.0.0 |
| **Networking** | Ktor Client 3.0.1 + KtorFit 2.6.0 |
| **Backend** | Firebase (Auth, Firestore, Storage, Crashlytics, Analytics) |
| **Authentication** | Google Sign-In (Credential Manager on Android, GIDSignIn on iOS) |
| **Async** | Kotlin Coroutines + Flow |
| **Images** | Coil |
| **Animations** | Lottie |
| **Code style** | ktlint |
| **Testing** | JUnit 4, Mockk, Turbine, Coroutines Test |

---

## Architecture

The project follows **Clean Architecture** with strict layer separation and the following dependency flow:

```
┌──────────────────────────────────────────────────────────┐
│      :app (Android UI)    │    :iosApp (iOS UI)          │
│     Jetpack Compose       │   Compose Multiplatform      │
└──────────────┬────────────┴────────────┬─────────────────┘
               │                         │
               └────────────┬────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                   :shared-ui                            │
│        Compose Multiplatform screens (commonMain)       │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                   :viewmodel                            │
│          ViewModel + StateFlow (commonMain)             │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                   :usecase                              │
│         Use Cases + Repository interfaces               │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                  :data:core                             │
│           Repository implementations                    │
└──────────────┬────────────────────────┬─────────────────┘
               │                        │
┌──────────────▼──────────┐  ┌──────────▼──────────────────┐
│      :data:local        │  │       :data:remote          │
│  Room (Android)         │  │  Ktor + Firebase (KMP)      │
│  NSUserDefaults (iOS)   │  │  Auth, Firestore, Storage   │
└─────────────────────────┘  └─────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    :domain                              │
│  Pure Kotlin — models, no dependencies                  │
│  All modules depend on this layer                       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                     :di                                 │
│  Koin composition root — aggregates all Koin modules    │
│  androidMain: ViewModelModule                           │
│  iosMain: IosModule                                     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   :service                              │
│  Background services (MatchNotificationController)      │
└─────────────────────────────────────────────────────────┘
```

> Repository interfaces are defined in `:usecase`; their implementations live in `:data:core`. `:data:core` delegates persistence to `:data:local` (Room on Android / NSUserDefaults on iOS) and remote operations to `:data:remote` (Ktor + Firebase via GitLive SDK).

### KMP code sharing

| Module | commonMain share |
|--------|-----------------|
| domain + usecase | ~100% |
| data:core | ~100% |
| data:local | ~85% |
| data:remote | ~75% |
| viewmodel | ~95% |
| shared-ui | ~90% |
| di | ~60% |
| **Total** | **~90%** |

---

## Dependency Injection

The project uses **Koin 4.0** with a module structure designed to enforce layer boundaries at the dependency graph level.

`:di` is the composition root: it declares a dependency on every other module and is the only place where all Koin module definitions are assembled into the application graph.

`:app` (Android) and `:iosApp` (iOS) declare compile-time dependencies exclusively on `:shared-ui`/`:viewmodel` and `:di`. This prevents the UI layer from referencing use cases, repositories, or data sources directly.

```
         ┌──────────────────────────────────────────────┐
         │                    :di                       │
         │  (composition root — depends on all modules) │
         └───┬──────┬──────┬──────┬──────┬──────┬───────┘
             │      │      │      │      │      │
           :app  :shared :view  :use  :data  :data  :data
           :ios   -ui   model  case  :core  :local :remote
```

Each module exposes its own Koin module definition (e.g., `UseCaseModule`, `DataCoreModule`) which `:di` collects and starts. Platform-specific bindings are in `ViewModelModule.kt` (Android) and `IosModule.kt` (iOS).

---

## Project Structure

```
TeamFlowManager/
├── app/                    # Android UI layer — Composables, Activities
├── iosApp/                 # iOS entry point — SwiftUI wrapper + Kotlin/Swift bridges
├── shared-ui/              # Compose Multiplatform screens (commonMain)
├── viewmodel/              # ViewModels and UI state (commonMain, StateFlow)
├── usecase/                # Use cases + repository interfaces
├── domain/                 # Domain models (pure Kotlin, no dependencies)
├── data/
│   ├── core/               # Repository implementations (DataSource interfaces)
│   ├── local/              # Room (Android) + NSUserDefaults (iOS) datasources
│   └── remote/             # Ktor + KtorFit + Firebase datasources (KMP)
├── di/                     # Koin composition root (androidMain + iosMain)
├── service/                # Android background services
├── gradle/
│   └── libs.versions.toml  # Centralized version catalog
├── .github/
│   └── workflows/
│       └── pr-checks.yml   # CI — build, tests, ktlint
├── firestore.rules         # Firestore security rules
└── firebase.json           # Firebase configuration
```

---

## Prerequisites

### Android
- **Android Studio** Ladybug (2024.2.1) or higher
- **JDK 17** (recommended: Eclipse Temurin)
- **Android SDK** with API 29+ installed
- Firebase project configured with `google-services.json` placed in `app/src/dev/` and `app/src/prod/`

### iOS
- **Xcode** 15 or higher
- **CocoaPods** (`gem install cocoapods`)
- `GoogleService-Info.plist` placed in `iosApp/iosApp/` (gitignored, download from Firebase Console)

---

## Installation

```bash
# 1. Clone the repository
git clone https://github.com/jesuslcorominas/teamflowmanager.git
cd teamflowmanager

# 2. Add the Firebase configuration files
cp google-services-dev.json  app/src/dev/google-services.json
cp google-services-prod.json app/src/prod/google-services.json

# 3. Open in Android Studio or build from the command line
./gradlew assembleDevDebug
```

For iOS, install CocoaPods dependencies first:

```bash
cd iosApp
pod install
# Then open iosApp.xcworkspace in Xcode (NOT .xcodeproj)
```

---

## Build

The project has two **product flavors**:

| Flavor | App ID suffix | Description |
|--------|--------------|-------------|
| `dev`  | `.dev`       | Development environment |
| `prod` | —            | Production environment |

```bash
# Debug (dev flavor)
./gradlew assembleDevDebug

# Debug (prod flavor)
./gradlew assembleProdDebug

# Release (requires keystore configured)
./gradlew assembleProdRelease

# Full build (all variants)
./gradlew build
```

---

## Testing

```bash
# All unit tests
./gradlew test

# Tests for a specific module
./gradlew :usecase:test
./gradlew :viewmodel:test
./gradlew :data:core:test

# A specific test class
./gradlew :usecase:test --tests "*.DeletePlayerUseCaseTest"

# Tests with detailed report
./gradlew test --continue
```

Tests use **JUnit 4**, **Mockk** for mocking, **Turbine** for Flow testing and **Coroutines Test** for async code.

---

## Code Coverage

JaCoCo is configured to generate a **unified report** across all modules with a single task.

```bash
# Generate coverage report for all modules
./gradlew testDebugUnitTestCoverage

# Report output:
#   HTML → build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
#   XML  → build/reports/jacoco/testDebugUnitTestCoverage/coverage.xml
```

Coverage is configured via three script plugins at the root:

| File | Purpose |
|------|---------|
| `jacoco.gradle.kts` | Applied to Android library/app modules |
| `jacoco-nonandroid.gradle.kts` | Applied to pure JVM/Kotlin modules |
| `jacoco-aggregate.gradle.kts` | Registers the unified `testDebugUnitTestCoverage` task at root |

### Adding a new module to coverage

**Step 1 — Apply the JaCoCo plugin** in the module's `build.gradle.kts`:

```kotlin
// Android module (com.android.library / com.android.application)
apply(from = "$rootDir/jacoco.gradle.kts")

// Pure JVM/Kotlin module
apply(from = "$rootDir/jacoco-nonandroid.gradle.kts")
```

**Step 2 — Add the module** to `jacoco-aggregate.gradle.kts`:

```kotlin
// Android library modules (no build flavors):
val androidModules = listOf(
    ":viewmodel",
    ":data:local",
    ":data:remote",
    ":your-new-module",   // ← add here
)

// Pure JVM/Kotlin modules:
val jvmModules = listOf(
    ":usecase",
    ":data:core",
    ":your-new-module",   // ← or here
)

// Android app modules with build flavors — specify the test task explicitly:
val androidAppModules = listOf(
    ":app" to "testDevDebugUnitTest",
    ":your-new-app" to "testDevDebugUnitTest",   // ← or here
)
```

---

## Code Style

```bash
# Check style
./gradlew ktlintCheck

# Auto-format
./gradlew ktlintFormat
```

ktlint is automatically applied to all subprojects via the root plugin.

---

## Continuous Integration

GitHub Actions runs the following checks on every Pull Request to `develop`:

| Check | Command |
|-------|---------|
| Build | `./gradlew build --no-daemon` |
| Unit Tests | `./gradlew test --no-daemon` |
| Code Style | `./gradlew ktlintCheck --no-daemon` |

Test results and build reports are published as artifacts on each run. The workflow runs on a self-hosted runner with JDK 17.

---

## License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.