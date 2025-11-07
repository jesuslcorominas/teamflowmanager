# Quick Fix Guide for Remaining Test Files

## RegisterPlayerSubstitutionUseCaseTest.kt

### Issues:
1. Uses `elapsedTimeMillis`, `isRunning`, `lastStartTimeMillis` - these don't exist in new Match model
2. Uses `matchRepository.getMatch()` which doesn't exist (should be `getMatchById(matchId)`)

### Fix Example:
```kotlin
// OLD:
val match = Match(
    id = matchId,
    elapsedTimeMillis = 5000L,
    isRunning = true,
    lastStartTimeMillis = 1000L,
    teamName = "Team A"
)
every { matchRepository.getMatch() } returns flowOf(match)

// NEW:
val match = Match(
    id = matchId,
    teamName = "Team A",
    opponent = "Team B",
    location = "Stadium A",
    periodType = PeriodType.HALF_TIME,
    captainId = 1L,
    status = MatchStatus.IN_PROGRESS,
    periods = listOf(
        MatchPeriod(
            periodNumber = 1,
            periodDuration = PeriodType.HALF_TIME.duration,
            startTimeMillis = 1000L,
            endTimeMillis = 0L  // 0 means still running
        )
    )
)
every { matchRepository.getMatchById(matchId) } returns flowOf(match)
```

## StartTimeoutUseCaseTest.kt

### Issues:
1. TransactionRunner mock is calling `block.invoke()` outside coroutine  
2. Need to fix the coEvery setup

### Fix:
```kotlin
@Before
fun setup() {
    matchRepository = mockk(relaxed = true)
    transactionRunner = mockk(relaxed = true)
    
    // Fix: Add <Unit> type parameter and make sure it's in coroutine context
    coEvery { transactionRunner.run<Unit>(any()) } coAnswers {
        val block = firstArg<suspend () -> Unit>()
        block.invoke()
    }
    
    startTimeoutUseCase = StartTimeoutUseCaseImpl(
        matchRepository,
        transactionRunner
    )
}
```

## RegisterGoalUseCaseTest.kt

### Issues:
Likely similar to RegisterPlayerSubstitutionUseCaseTest - using old Match properties

### Steps:
1. Add missing imports: `PeriodType`, `MatchStatus`, `MatchPeriod`
2. Replace all Match instantiations with new required parameters
3. Replace `elapsedTimeMillis` tracking with `periods` list
4. Update `getMatch()` to `getMatchById(matchId)`
5. Add TransactionRunner if needed

## Common Patterns

### Creating a Match for Tests:
```kotlin
// Helper function already exists in TestHelpers.kt
import com.jesuslcorominas.teamflowmanager.domain.model.createTestMatch

val match = createTestMatch(
    id = 1L,
    teamName = "Team A",
    // only override what you need to test
    status = MatchStatus.IN_PROGRESS,
    periods = listOf(
        MatchPeriod(1, startTimeMillis = 1000L)
    )
)
```

### TransactionRunner Mock Setup:
```kotlin
private lateinit var transactionRunner: TransactionRunner

@Before
fun setup() {
    transactionRunner = mockk(relaxed = true)
    
    coEvery { transactionRunner.run<Unit>(any()) } coAnswers {
        val block = firstArg<suspend () -> Unit>()
        block.invoke()
    }
}
```

### Repository Method Updates:
```kotlin
// OLD
matchRepository.getMatch()
matchRepository.startTimer(currentTime)
matchRepository.pauseTimer(currentTime)

// NEW  
matchRepository.getMatchById(matchId)
matchRepository.startTimer(matchId, currentTimeMillis)
matchRepository.pauseTimer(matchId, currentTimeMillis)
```

## Testing Individual Files

```bash
# Test a single file
cd /home/runner/work/teamflowmanager/teamflowmanager
./gradlew :usecase:test --tests "*RegisterPlayerSubstitutionUseCaseTest"
./gradlew :usecase:test --tests "*StartTimeoutUseCaseTest"
./gradlew :usecase:test --tests "*RegisterGoalUseCaseTest"

# Check compilation only
./gradlew :usecase:compileTestKotlin
```

## Validation Checklist

After fixing each file:
- [ ] File compiles without errors
- [ ] All tests in the file pass
- [ ] No deprecated API usage
- [ ] Follows same patterns as other fixed tests
- [ ] Import statements are clean (no unused imports)
