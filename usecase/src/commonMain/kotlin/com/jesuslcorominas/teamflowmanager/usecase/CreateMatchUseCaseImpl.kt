package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.SkeletonMatch
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class CreateMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
) : CreateMatchUseCase {
    override suspend fun invoke(skeleton: SkeletonMatch): Long {
        val team = teamRepository.getTeam().first() ?: throw IllegalStateException("Cannot create match without a team")

        return matchRepository.createMatch(skeleton.toMatch(team))
    }
}

private fun SkeletonMatch.toMatch(team: Team): Match {
    return Match(
        teamId = team.id,
        teamName = team.name,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        periodType = PeriodType.fromNumberOfPeriods(numberOfPeriods),
        squadCallUpIds = this.squadCallUpIds,
        captainId = this.captainId,
        startingLineupIds = this.startingLineupIds,
    )
}
