package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Helper function to create a Match for testing purposes with default values
 */
fun createTestMatch(
    id: Long = 0L,
    teamId: Long = 1L,
    teamName: String = "Test Team",
    opponent: String = "Test Opponent",
    location: String = "Test Location",
    dateTime: Long? = null,
    periodType: PeriodType = PeriodType.HALF_TIME,
    squadCallUpIds: List<Long> = emptyList(),
    captainId: Long = 1L,
    startingLineupIds: List<Long> = emptyList(),
    status: MatchStatus = MatchStatus.SCHEDULED,
    archived: Boolean = false,
    pauseCount: Int = 0,
    goals: Int = 0,
    opponentGoals: Int = 0,
    timeoutStartTimeMillis: Long = 0L,
    periods: List<MatchPeriod> = (1..periodType.numberOfPeriods).map {
        MatchPeriod(
            periodNumber = it,
            periodDuration = periodType.duration
        )
    }
): Match = Match(
    id = id,
    teamId = teamId,
    teamName = teamName,
    opponent = opponent,
    location = location,
    dateTime = dateTime,
    periodType = periodType,
    squadCallUpIds = squadCallUpIds,
    captainId = captainId,
    startingLineupIds = startingLineupIds,
    status = status,
    archived = archived,
    pauseCount = pauseCount,
    goals = goals,
    opponentGoals = opponentGoals,
    timeoutStartTimeMillis = timeoutStartTimeMillis,
    periods = periods
)
