# TeamFlow Manager
⚽ TeamFlow Manager: App de gestión de equipos de fútbol infantil. Controla minutos, cambios, asistencias y estadísticas en tiempo real. Desarrollada con Kotlin Multiplatform (KMM) para Android e iOS. La herramienta esencial para el entrenador.

## Project Structure

This project follows a **Kotlin Multiplatform Mobile (KMM)** architecture with clean separation of concerns and shared business logic between Android and iOS platforms.

### Modules

#### Shared KMM Modules (Cross-Platform)

##### :shared
- **Type**: Kotlin Multiplatform Library
- **Purpose**: Main shared module that aggregates all shared code
- **Targets**: Android, iOS (arm64, x64, simulator)
- **Dependencies**: `:shared:domain`, `:shared:usecase`, `:shared:data:core`, `:shared:data:remote`

##### :shared:domain
- **Type**: Kotlin Multiplatform Library
- **Purpose**: Contains domain models and interfaces (entities, repositories)
- **Technologies**: Kotlin, Coroutines
- **Dependencies**: None (pure domain layer)

##### :shared:usecase
- **Type**: Kotlin Multiplatform Library
- **Purpose**: Contains business logic and use cases
- **Technologies**: Kotlin, Coroutines, Koin
- **Dependencies**: `:shared:domain`

##### :shared:data:core
- **Type**: Kotlin Multiplatform Library
- **Purpose**: Core data layer abstractions and repository implementations
- **Technologies**: Kotlin, Coroutines, Koin
- **Dependencies**: `:shared:domain`, `:shared:usecase`

##### :shared:data:remote
- **Type**: Kotlin Multiplatform Library
- **Purpose**: Remote data source implementation using Ktor
- **Technologies**: Ktor Client (OkHttp on Android, Darwin on iOS), kotlinx.serialization
- **Dependencies**: `:shared:domain`, `:shared:data:core`

#### Platform-Specific Modules

##### :androidApp
- **Type**: Android Application module
- **Purpose**: Contains the Android UI layer of the application
- **Technologies**: Android SDK, Jetpack Compose, Koin for DI
- **Dependencies**: `:shared`, `:viewmodel`, `:di`

##### :viewmodel
- **Type**: Android Library
- **Purpose**: Contains ViewModels and presentation logic (Android-specific)
- **Technologies**: Android ViewModel, LiveData, Coroutines
- **Dependencies**: `:shared:usecase`, `:shared:domain`

##### :service
- **Type**: Android Library
- **Purpose**: Contains Android services (notifications, background tasks)
- **Technologies**: Android Services, Coroutines
- **Dependencies**: `:shared:usecase`, `:shared:domain`

##### :data:local
- **Type**: Android Library
- **Purpose**: Local data source implementation using Room
- **Technologies**: Room Database, Coroutines
- **Dependencies**: `:shared:data:core`, `:shared:domain`

##### :di
- **Type**: Android Library
- **Purpose**: Dependency injection configuration using Koin for Android
- **Technologies**: Koin
- **Dependencies**: `:viewmodel`, `:service`, `:data:local`, `:shared:usecase`, `:shared:data:core`, `:shared:data:remote`

##### iosApp/
- **Type**: iOS Application (Xcode Project)
- **Purpose**: Contains the iOS UI layer of the application
- **Technologies**: SwiftUI, UIKit
- **Dependencies**: `:shared` (via framework)

## Build Configuration

The project uses:
- **Gradle Version**: 8.11
- **Kotlin Version**: 2.1.0
- **Android Gradle Plugin**: 8.6.1
- **Compose Multiplatform**: 1.7.3
- **Ktor**: 3.0.1
- **Koin**: 4.0.0
- **Min SDK (Android)**: 29
- **Target SDK (Android)**: 36
- **iOS Deployment Target**: 14.1

### Gradle Version Catalog

Dependencies are managed through Gradle Version Catalog (`gradle/libs.versions.toml`) for consistent dependency management across all modules.

## Building the Project

### Android

```bash
./gradlew build
```

### Building the Android APK

```bash
./gradlew assembleDebug
```

### Building iOS Shared Framework

```bash
./gradlew :shared:linkDebugFrameworkIosArm64
# or for simulator
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### Running Tests

```bash
./gradlew test
```

### Running ktlint

Check code style:

```bash
./gradlew ktlintCheck
```

Auto-format code:

```bash
./gradlew ktlintFormat
```

## iOS Development

1. Build the shared framework:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

2. Open the iOS project in Xcode:
```bash
open iosApp/iosApp.xcodeproj
```

3. Build and run the iOS app from Xcode

## Continuous Integration

The project uses GitHub Actions for continuous integration. On every pull request to `main` or `develop` branches, the following checks are automatically run:

- **Build**: Compiles the entire project
- **Tests**: Runs all unit tests
- **Lint**: Runs ktlint to check Kotlin code style

The CI workflow is defined in `.github/workflows/pr-checks.yml`.

## Architecture

The project follows Clean Architecture principles with Kotlin Multiplatform support:

```
┌─────────────────────────────────────────────────────────────────┐
│                      Platform Applications                       │
├──────────────────────────────┬──────────────────────────────────┤
│        :androidApp           │           iosApp/                 │
│   (Jetpack Compose, Koin)    │     (SwiftUI, UIKit)             │
└──────────────┬───────────────┴──────────────┬───────────────────┘
               │                              │
               │    ┌─────────────────────────┼─────────────────┐
               │    │    Platform-Specific    │                 │
               ├────┤       Modules           │                 │
               │    │  :viewmodel, :service   │                 │
               │    │      :data:local        │                 │
               │    └─────────────────────────┘                 │
               │                                                │
               └────────────────┬───────────────────────────────┘
                                │
┌───────────────────────────────┴──────────────────────────────────┐
│                      :shared (KMM)                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                   commonMain                                 │ │
│  ├─────────────────────────────────────────────────────────────┤ │
│  │  :shared:domain     - Domain models, Repository interfaces  │ │
│  │  :shared:usecase    - Business logic and use cases          │ │
│  │  :shared:data:core  - Repository implementations            │ │
│  │  :shared:data:remote- Remote API (Ktor)                     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────┬──────────────────────────────────────┐ │
│  │     androidMain      │              iosMain                  │ │
│  │   (Ktor OkHttp)      │         (Ktor Darwin)                │ │
│  └──────────────────────┴──────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

## Kotlin Multiplatform (KMM) Features

### ✅ Shared Components (Android & iOS)
- **Domain Models**: All business entities (Player, Match, Team, etc.)
- **Use Cases**: All business logic
- **Repository Interfaces**: Data access abstractions
- **Repository Implementations**: Data layer logic
- **Remote API**: Ktor-based networking
- **Dependency Injection**: Koin (multiplatform)

### 🔄 Platform-Specific Components
- **Android**:
  - UI Layer: Jetpack Compose
  - Local Storage: Room Database
  - ViewModel: Android Architecture Components
  - Services: Background services for notifications
  
- **iOS**:
  - UI Layer: SwiftUI
  - Local Storage: (Future: Core Data or SQLDelight)
  - Framework integration via Kotlin/Native

### 🎯 Code Sharing Approach
- ~70% shared code in `commonMain`
- Platform-specific implementations using `expect/actual` pattern
- HTTP engine: OkHttp (Android) / Darwin (iOS)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
