package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Resolves match period configuration from the team type.
 *
 * Only Football 5 lets the coach choose between 2 halves and 4 quarters. Football 7, 8 and 11
 * have a fixed regulation format, so the wizard does not ask and the duration comes from here.
 */
object MatchPeriodRules {
    fun isPeriodTypeSelectable(teamType: TeamType): Boolean = teamType == TeamType.FOOTBALL_5

    fun defaultPeriodType(teamType: TeamType): PeriodType = PeriodType.HALF_TIME

    fun periodDuration(
        teamType: TeamType,
        periodType: PeriodType,
    ): Long =
        when (teamType) {
            TeamType.FOOTBALL_5 -> periodType.duration
            TeamType.FOOTBALL_7, TeamType.FOOTBALL_8 -> MatchPeriod.PERIOD_DURATION_TWO_HALF_30
            TeamType.FOOTBALL_11 -> MatchPeriod.PERIOD_DURATION_TWO_HALF_45
        }

    /**
     * Forces the regulation period type when the team type does not allow choosing, so a stale or
     * hand-crafted `numberOfPeriods` can never produce a 4-quarter Football 11 match.
     */
    fun resolvePeriodType(
        teamType: TeamType,
        requested: PeriodType,
    ): PeriodType = if (isPeriodTypeSelectable(teamType)) requested else defaultPeriodType(teamType)

    fun buildPeriods(
        teamType: TeamType,
        periodType: PeriodType,
    ): List<MatchPeriod> {
        val resolved = resolvePeriodType(teamType, periodType)
        val duration = periodDuration(teamType, resolved)
        return (1..resolved.numberOfPeriods).map {
            MatchPeriod(periodNumber = it, periodDuration = duration)
        }
    }
}
