# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build the project
./gradlew build --no-daemon --stacktrace

# Run all unit tests
./gradlew test --no-daemon --stacktrace

# Run tests for a specific module
./gradlew :usecase:test
./gradlew :domain:test

# Run a single test class
./gradlew :usecase:test --tests "com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCaseTest"

# Lint check (ktlint)
./gradlew ktlintCheck --no-daemon --stacktrace

# Auto-format code (ktlint)
./gradlew ktlintFormat
```

Build flavors: **dev** (suffix `.dev`, `-dev`) and **prod**.

## Architecture Overview

This is a Clean Architecture Android app with strict module boundaries:

```
domain      →  Pure Kotlin. Models, repository interfaces, use case interfaces.
usecase     →  Pure Kotlin. Use case implementations. Defines repository interfaces (not data layer's).
data/core   →  Repository implementations. Bridges usecase interfaces to DataSource abstractions.
data/local  →  Room database DAOs and datasource implementations.
data/remote →  KtorFit + Firebase (Firestore, Storage, Auth) datasource implementations.
service     →  Android services and cross-cutting concerns (MatchNotificationController).
viewmodel   →  Jetpack ViewModel + Flow-based UI state management.
di          →  Koin DI composition root — all modules wired here.
app         →  Jetpack Compose UI, MainActivity, app initialization.
```

**Critical interface layering**: `usecase/` defines its own repository interfaces (e.g., `MatchRepository`). `data/core/` implements those interfaces using DataSources. `data/local/` and `data/remote/` implement those DataSources. This means the use case layer has zero dependency on the data layer.

## Key Patterns

**Use case pattern**: Each use case is an interface + `Impl` class. The `Impl` takes repository interfaces via constructor injection. Tests mock the repositories with MockK.

**Repository pattern**: `data/core` repositories implement interfaces from `usecase`, delegate to one or more DataSource interfaces (defined in `data/core`), which are implemented in `data/local` and `data/remote`.

**Reactive streams**: Use cases return `Flow<T>`. ViewModels collect flows and expose `StateFlow<UiState>` to Compose.

**Dependency injection**: Koin. All module definitions are composed in `:di`. Each module has its own `*Module.kt` file (e.g., `UseCaseModule.kt`, `DataCoreModule.kt`).

**Atomic match operations**: `MatchOperationRepository` tracks operation IDs to prevent duplicate operations. The `lastCompletedOperationId` field in `Match` is critical for preventing concurrent state inconsistencies.

## Testing Patterns

Tests live in `src/test/kotlin/` alongside source. The standard pattern:

```kotlin
class SomeUseCaseTest {
    private lateinit var someRepository: SomeRepository
    private lateinit var useCase: SomeUseCase

    @Before
    fun setup() {
        someRepository = mockk(relaxed = true)
        useCase = SomeUseCaseImpl(someRepository)
    }

    @Test
    fun `description of behavior`() = runTest {
        // Given
        every { someRepository.someMethod() } returns flowOf(someValue)
        // When
        val result = useCase.invoke().toList()
        // Then
        assertEquals(expected, result[0])
        verify { someRepository.someMethod() }
    }
}
```

Libraries: JUnit 4, MockK (`mockk(relaxed = true)`), kotlinx-coroutines-test (`runTest`), Turbine for complex Flow testing.

## Domain Model Highlights

- **Club → Team → Player** hierarchy with multiple roles (Owner, Coach, President, Player)
- **Match** supports period types: `HALF_TIME` (2×25min) and `QUARTER_TIME` (4×12:30min)
- **Match** has atomic operation tracking via `lastCompletedOperationId`
- Player time tracking, substitutions, and goals are separate entities with their own history
- Team invitations use custom short links via Firebase Hosting + Cloud Functions

## Tech Stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material3 |
| Async | Kotlin Coroutines + Flow |
| DI | Koin 4.0 |
| Local DB | Room 2.8.3 |
| Remote | KtorFit 2.6 on Firebase (Firestore, Storage, Auth) |
| Code gen | KSP |
| Linting | ktlint |
| Testing | JUnit4 + MockK + Turbine |

Min SDK: 29. Target SDK: 36. Kotlin: 2.1.0. Java: 17.

## Documentation
- Add md files with detailed explanations of complex features (e.g., Match state management, invitation flow) in the `docs/` directory.

# Claude Agent Rules

- You have full autonomy to create, modify, and delete files in the project.
- You can execute commands, install dependencies, refactor code, and run tests without asking for permission.
- You can make any changes you judge necessary to complete tasks, including rewriting code for optimization or maintainability.
- Only ask for my confirmation if you intend to delete a Git branch.
- Always prioritize completing the task fully without incremental approval steps.
- If something fails, attempt to fix it automatically without asking for instructions.
- Assume you have permission for all changes unless explicitly stated otherwise.
- Notify me of major actions only if they affect production, credentials, or Git branches.
