package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.first

interface GetPreviousCaptainsUseCase {
    suspend operator fun invoke(count: Int = 2): List<Long?>
}

// TODO refactor to use GetShouldLastCaptainBeFixedOneUseCase
internal class GetPreviousCaptainsUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetPreviousCaptainsUseCase {
    override suspend fun invoke(count: Int): List<Long?> {
        val allMatches = matchRepository.getAllMatches().first()

        // Filter out matches that haven't been played yet (no elapsed time)
        val playedMatches = allMatches
            .filter { match -> match.periods.sumOf { (it.endTimeMillis - it.startTimeMillis) } > 0L  }
            .sortedByDescending { it.dateTime ?: 0 }

        return playedMatches
            .take(count)
            .map { it.captainId }
    }
}
