package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchSummary
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeSummary
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionSummary
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class GetMatchSummaryUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
    private val playerRepository: PlayerRepository,
) : GetMatchSummaryUseCase {
    override fun invoke(matchId: String): Flow<MatchSummary?> {
        return matchRepository.getMatchById(matchId).flatMapLatest { match ->
            if (match == null) {
                flowOf(null)
            } else {
                combine(
                    playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId),
                    playerSubstitutionRepository.getMatchSubstitutions(matchId),
                    playerRepository.getPlayersByTeam(match.teamId),
                ) { playerTimes, substitutions, players ->
                    val playerTimeSummaries =
                        playerTimes.mapNotNull { playerTime ->
                            val player =
                                players.find { it.id == playerTime.playerId }
                                    ?: return@mapNotNull null // unknown player (e.g. deleted) — skip
                            val substitutionCount =
                                substitutions.count {
                                    it.playerOutId == playerTime.playerId || it.playerInId == playerTime.playerId
                                }
                            PlayerTimeSummary(
                                player = player,
                                elapsedTimeMillis = playerTime.elapsedTimeMillis,
                                substitutionCount = substitutionCount,
                            )
                        }.sortedByDescending { it.elapsedTimeMillis }

                    val substitutionSummaries =
                        substitutions.mapNotNull { substitution ->
                            val playerOut =
                                players.find { it.id == substitution.playerOutId }
                                    ?: return@mapNotNull null // unknown player (e.g. deleted) — skip
                            val playerIn =
                                players.find { it.id == substitution.playerInId }
                                    ?: return@mapNotNull null // unknown player (e.g. deleted) — skip
                            SubstitutionSummary(
                                playerOut = playerOut,
                                playerIn = playerIn,
                                matchElapsedTimeMillis = substitution.matchElapsedTimeMillis,
                            )
                        }.sortedBy { it.matchElapsedTimeMillis }

                    MatchSummary(
                        match = match,
                        playerTimes = playerTimeSummaries,
                        substitutions = substitutionSummaries,
                    )
                }
            }
        }
    }
}
