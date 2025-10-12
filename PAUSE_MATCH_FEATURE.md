# Pause Match Functionality

This document describes the implementation of the pause match feature (US-2.1.8).

## Overview

The pause match functionality allows a coach to pause an in-progress match, which:
- Pauses all active timers (match timer and player timers)
- Consolidates the first half time in the database
- Allows resuming the match later for the second half
- Keeps the match in "in progress" state (paused) so no other match can start

## Architecture

The implementation follows clean architecture with the following layers:

### Domain Layer (`domain` module)
- **Match.kt**: Domain model representing a match with status (NOT_STARTED, IN_PROGRESS, PAUSED, FINISHED)
- **PlayerTimer.kt**: Domain model for tracking individual player time
- **MatchRepository.kt**: Repository interface defining match operations

### Use Case Layer (`usecase` module)
- **PauseMatchUseCase.kt**: Use case for pausing a match
- **ResumeMatchUseCase.kt**: Use case for resuming a paused match
- **GetMatchUseCase.kt**: Use case for retrieving match details

### Data Layer (`data:core` module)
- **MatchRepositoryImpl.kt**: In-memory implementation of MatchRepository

### Presentation Layer
- **MatchDetailViewModel.kt** (`viewmodel` module): ViewModel managing match detail state
- **MatchDetailScreen.kt** (`app` module): UI screen showing match details with pause/resume controls

### Dependency Injection (`di` module)
- **MatchModule.kt**: Koin module configuring dependencies

## Usage

### UI Flow
1. User navigates to match detail screen
2. When match is IN_PROGRESS, a "Descanso" (Break/Halftime) button is shown
3. Clicking "Descanso" pauses the match
4. When match is PAUSED, a "Resume" button is shown
5. Clicking "Resume" resumes the match

### Code Example

```kotlin
// Pause a match
val result = pauseMatchUseCase("matchId")
result.onSuccess { pausedMatch ->
    // Match successfully paused
    // pausedMatch.status == MatchStatus.PAUSED
    // All player timers are inactive
}

// Resume a match
val result = resumeMatchUseCase("matchId")
result.onSuccess { resumedMatch ->
    // Match successfully resumed
    // resumedMatch.status == MatchStatus.IN_PROGRESS
}
```

## Testing

Unit tests are provided in:
- `usecase/src/test/kotlin/.../PauseMatchUseCaseTest.kt`

Tests verify:
- Successfully pausing an in-progress match
- Failing to pause a match that is not in progress
- Failing to pause a non-existent match

## Future Enhancements

- Persist match data to Room database
- Add actual timer implementation with coroutines
- Implement match time tracking in real-time
- Add player substitution tracking
- Add match statistics
