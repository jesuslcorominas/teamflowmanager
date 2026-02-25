# TeamFlow Manager

> ⚽ App de gestión de equipos de fútbol infantil. Controla minutos, cambios, asistencias y estadísticas en tiempo real. La herramienta esencial para el entrenador.

[![CI](https://github.com/jesuslcorominas/teamflowmanager/actions/workflows/pr-checks.yml/badge.svg)](https://github.com/jesuslcorominas/teamflowmanager/actions/workflows/pr-checks.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin)
![Android](https://img.shields.io/badge/Min%20SDK-29-green?logo=android)

---

## Features

- **Minute tracking** — Individual player stopwatch accumulating real playing time
- **Substitution management** — Substitution recording with automatic time control
- **Match timer** — Global stopwatch with pause and resume support
- **Team management** — Add, edit and remove players and teams
- **Real-time statistics** — Playing time and participation visualization during the match
- **Multi-user support** — Google Sign-In authentication with data synced via Firestore
- **Deep linking** — Direct access to active match (`teamflowmanager://match`) and team invitation (`teamflowmanager://team/accept`)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin 2.1.0 |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | Clean Architecture + MVVM |
| **DI** | Koin 4.0.0 |
| **Networking** | Ktor Client 3.0.1 + KtorFit 2.6.0 |
| **Backend** | Firebase (Auth, Firestore, Storage, Crashlytics, Analytics) |
| **Authentication** | Google Sign-In (Credential Manager) |
| **Async** | Kotlin Coroutines + Flow |
| **Images** | Coil |
| **Animations** | Lottie |
| **Code style** | ktlint |
| **Testing** | JUnit 4, Mockk, Turbine, Coroutines Test |

---

## Architecture

The project follows **Clean Architecture** with strict layer separation and the following dependency flow:

```
┌─────────────────────────────────────┐
│            :app  (UI Layer)         │
│       Jetpack Compose + Material 3  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      :viewmodel  (Presentation)     │
│        ViewModel + StateFlow        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    :usecase  (Business Logic)       │
│     Use Cases + Repository ifaces   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     :data:core  (Data Layer)        │
│     Repository implementations      │
└──────────┬───────────┬──────────────┘
           │           │
┌──────────▼───────┐ ┌─▼────────────────────┐
│   :data:local    │ │     :data:remote     │
│  Room database   │ │  Ktor + Firebase/API │
└──────────────────┘ └──────────────────────┘

┌─────────────────────────────────────┐
│        :domain  (Domain Models)     │
│  Pure Kotlin — no dependencies      │
│  All modules depend on this layer   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        :di  (Koin DI wiring)        │
│  Aggregates all Koin modules        │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        :service                     │
│  Background services                │
└─────────────────────────────────────┘
```

> Repository interfaces are defined in `:usecase`; their implementations live in `:data:core`. Implementations are `internal` to their module. `:data:core` delegates persistence to `:data:local` (Room) and remote operations to `:data:remote` (Ktor + Firebase).

---

## Dependency Injection

The project uses **Koin 4.0** with a module structure designed to enforce layer boundaries at the dependency graph level.

`:di` is the composition root: it declares a dependency on every other module and is the only place where all Koin module definitions are assembled into the application graph. No other module has visibility into implementation details outside its own layer.

`:app` declares compile-time dependencies exclusively on `:viewmodel` and `:di`. This constraint prevents the UI layer from referencing use cases, repositories, or data sources directly — access to lower layers is only possible through the ViewModels and the injected graph.

```
         ┌──────────────────────────────────────────────┐
         │                    :di                       │
         │  (composition root — depends on all modules) │
         └───┬──────┬──────┬──────┬──────┬──────┬───────┘
             │      │      │      │      │      │
           :app  :view  :use  :data  :data  :data
                  model  case  :core  :local :remote
             │
         (also depends on :viewmodel for ViewModels
          and :di to load the Koin graph at startup)
```

Each module exposes its own Koin module definition (e.g., `UseCaseModule`, `DataCoreModule`) which `:di` collects and starts. Adding a new module requires only declaring its Koin module and registering it in `:di` — no changes are needed elsewhere.

---

## Project Structure

```
TeamFlowManager/
├── app/                    # UI layer — Composables, Activities
├── viewmodel/              # ViewModels and UI state (StateFlow)
├── usecase/                # Use cases + repository interfaces
├── domain/                 # Domain models (pure Kotlin, no dependencies)
├── data/
│   ├── core/               # Repository implementations (DataSource interfaces)
│   ├── local/              # Room database — DAOs and local DataSource implementations
│   └── remote/             # Ktor client + KtorFit + Firebase DataSource implementations
├── di/                     # Koin configuration (AppModule + submodules)
├── service/                # Background services
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

- **Android Studio** Ladybug (2024.2.1) or higher
- **JDK 17** (recommended: Eclipse Temurin)
- **Android SDK** with API 29+ installed
- Firebase project configured with `google-services.json` placed in `app/`

---

## Installation

```bash
# 1. Clone the repository
git clone https://github.com/jesuslcorominas/teamflowmanager.git
cd teamflowmanager

# 2. Add the Firebase configuration file
#    Download it from Firebase Console and place it at:
cp google-services.json app/

# 3. Open in Android Studio or build from the command line
./gradlew assembleDevDebug
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

GitHub Actions runs the following checks on every Pull Request to `main`:

| Check | Command |
|-------|---------|
| Build | `./gradlew build --no-daemon` |
| Unit Tests | `./gradlew test --no-daemon` |
| Code Style | `./gradlew ktlintCheck --no-daemon` |

Test results and build reports are published as artifacts on each run. The workflow runs on a self-hosted runner with JDK 17.

---

## License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.
