# TeamFlow Manager

⚽ TeamFlow Manager: App de gestión de equipos de fútbol infantil. Controla minutos, cambios, asistencias y estadísticas en tiempo real. Desarrollada con Kotlin Multiplatform (KMM) con migración a Firestore planificada. La herramienta esencial para el entrenador.

## Project Structure

This project follows a modular architecture with clean separation of concerns:

### Modules

#### :app
- **Type**: Android Application module
- **Purpose**: Contains the UI layer of the application
- **Technologies**: Android SDK, Jetpack Compose/View Binding, Hilt for DI
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
- **Purpose**: Dependency injection configuration using Hilt
- **Technologies**: Hilt/Dagger
- **Dependencies**: `:usecase`, `:data:core`, `:data:local`, `:data:remote`, `:domain`

## Build Configuration

The project uses:
- **Gradle Version**: 8.2
- **Kotlin Version**: 1.9.10
- **Android Gradle Plugin**: 8.1.2
- **Min SDK**: 24
- **Target SDK**: 34
- **Compile SDK**: 34

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

### Building the APK

```bash
./gradlew assembleDebug
```

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
│         depends on: usecase         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    :usecase (Business Logic)        │
│      depends on: viewmodel          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     :data:core (Data Repository)    │
│        depends on: usecase          │
└──────────────┬──────────────────────┘
       ┌───────┴────────┐
       │                │
┌──────▼─────┐   ┌──────▼──────┐
│ :data:local│   │ :data:remote│
│depends on: │   │depends on:  │
│ data:core  │   │  data:core  │
└────────────┘   └─────────────┘

┌─────────────────────────────────────┐
│        :di (Dependency Injection)   │
│  depends on: viewmodel, usecase,    │
│  data:core, data:local, data:remote │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        :domain (Domain Models)      │
│      Pure domain layer - no deps    │
│   All modules depend on domain      │
└─────────────────────────────────────┘
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
