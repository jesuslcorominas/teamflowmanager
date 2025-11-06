# TeamFlow Manager
⚽ TeamFlow Manager: App de gestión de equipos de fútbol infantil. Controla minutos, cambios, asistencias y estadísticas en tiempo real. Desarrollada con Kotlin Multiplatform (KMM) con migración a Firestore planificada. La herramienta esencial para el entrenador.

## Project Structure

This project follows a modular architecture with clean separation of concerns:

### Modules

#### :app
- **Type**: Android Application module
- **Purpose**: Contains the UI layer of the application
- **Technologies**: Android SDK, Jetpack Compose/View Binding, Koin for DI
- **Dependencies**: `:viewmodel`, `:di`

#### :viewmodel
- **Type**: Android Library
- **Purpose**: Contains ViewModels and presentation logic
- **Technologies**: Android ViewModel, LiveData, Coroutines
- **Dependencies**: `:usecase`, `:domain`

#### :usecase
- **Type**: Kotlin Library (Pure JVM)
- **Purpose**: Contains business logic and use cases
- **Technologies**: Kotlin, Coroutines
- **Dependencies**: `:domain`

#### :domain
- **Type**: Kotlin Library (Pure JVM)
- **Purpose**: Contains domain models and interfaces (entities, repositories)
- **Technologies**: Kotlin
- **Dependencies**: None (pure domain layer)

#### :data:core
- **Type**: Kotlin Library (Pure JVM)
- **Purpose**: Core data layer abstractions and repository implementations
- **Technologies**: Kotlin, Coroutines
- **Dependencies**: `:domain`

#### :data:local
- **Type**: Android Library
- **Purpose**: Local data source implementation using Room
- **Technologies**: Room Database, Coroutines
- **Dependencies**: `:data:core`, `:domain`

#### :data:remote
- **Type**: Kotlin Library (Pure JVM)
- **Purpose**: Remote data source implementation using Retrofit
- **Technologies**: Retrofit, OkHttp, Gson
- **Dependencies**: `:data:core`, `:domain`

#### :di
- **Type**: Android Library
- **Purpose**: Dependency injection configuration using Koin
- **Technologies**: Koin
- **Dependencies**: `:usecase`, `:data:core`, `:data:local`, `:data:remote`, `:domain`

## Build Configuration

The project uses:
- **Gradle Version**: 8.11.1
- **Kotlin Version**: 2.1.0
- **Android Gradle Plugin**: 8.6.1
- **Compose Multiplatform**: 1.7.3 (configured, ready for multiplatform UI)
- **Min SDK**: 29
- **Target SDK**: 36
- **Compile SDK**: 36

### Gradle Version Catalog

Dependencies are managed through Gradle Version Catalog (`gradle/libs.versions.toml`) for consistent dependency management across all modules.

## Building the Project

```bash
./gradlew build
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

### Building the APK

```bash
./gradlew assembleDebug
```

## Continuous Integration

The project uses GitHub Actions for continuous integration. On every pull request to `main` or `develop` branches, the following checks are automatically run:

- **Build**: Compiles the entire project
- **Tests**: Runs all unit tests
- **Lint**: Runs ktlint to check Kotlin code style

The CI workflow is defined in `.github/workflows/pr-checks.yml`.

## Architecture

The project follows Clean Architecture principles with the following dependency flow:

```
┌─────────────────────────────────────┐
│            :app (UI Layer)          │
│         depends on: viewmodel, di   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      :viewmodel (Presentation)      │
│      depends on: usecase, domain    │
└─────────────────────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    :usecase (Business Logic)        │
│         depends on: domain          │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│     :data:core (Data Repository)    │
│         depends on: domain          │
└──────────────┬──────────────────────┘
       ┌───────┴────────┐
       │                │
┌──────▼─────┐   ┌──────▼──────┐
│ :data:local│   │ :data:remote│
│depends on: │   │depends on:  │
│data:core,  │   │data:core,   │
│  domain    │   │   domain    │
└────────────┘   └─────────────┘

┌─────────────────────────────────────┐
│        :di (Dependency Injection)   │
│  depends on: viewmodel, usecase,    │
│  data:core, data:local, data:remote │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        :domain (Domain Models)      │
│      Pure domain layer - no deps    │
│   All modules (except :di) depend   │
│          on :domain                 │
└─────────────────────────────────────┘
```

## Kotlin Multiplatform (KMM) Readiness

The project is being prepared for Kotlin Multiplatform with the following components already compatible:

### ✅ Multiplatform-Ready Components
- **Dependency Injection**: Koin 4.0.0 (fully multiplatform)
- **Network Layer**: Ktor Client 3.0.1 + KtorFit 2.6.0 (KMP-compatible)
- **Business Logic**: Pure Kotlin modules (`:usecase`, `:domain`, `:data:core`, `:data:remote`)
- **UI Framework**: Compose Multiplatform 1.7.3 plugin configured (ready for shared UI)

### 🔄 Platform-Specific Components
- **UI Layer**: Currently Android-only with Jetpack Compose
- **Local Storage**: Room (Android-specific, would need SQLDelight for KMP)
- **ViewModel**: Android-specific implementation

### 🎯 Future Multiplatform Migration Path
When adding iOS/Desktop/Web support:
1. Create KMP module structure (`commonMain`, `androidMain`, `iosMain`)
2. Migrate shared UI components to Compose Multiplatform
3. Replace Room with SQLDelight for cross-platform database
4. Share business logic, data layer, and UI components across platforms

For more details, see:
- [US-7.1.5 Implementation Summary](US-7.1.5_IMPLEMENTATION_SUMMARY.md) - Koin Multiplatform migration
- [US-7.1.6 Implementation Summary](US-7.1.6_IMPLEMENTATION_SUMMARY.md) - Compose Multiplatform analysis

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
