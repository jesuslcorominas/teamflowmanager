package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class MatchSummary(
    val match: Match,
    val playerTimes: List<PlayerTimeSummary>,
    val substitutions: List<SubstitutionSummary>,
)

data class PlayerTimeSummary(
    val player: Player,
    val elapsedTimeMillis: Long,
)

data class SubstitutionSummary(
    val playerOut: Player,
    val playerIn: Player,
    val matchElapsedTimeMillis: Long,
)

interface GetMatchSummaryUseCase {
    operator fun invoke(matchId: Long): Flow<MatchSummary?>
}

internal class GetMatchSummaryUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
    private val playerRepository: PlayerRepository,
) : GetMatchSummaryUseCase {
    override fun invoke(matchId: Long): Flow<MatchSummary?> {
        return combine(
            matchRepository.getMatchById(matchId),
            playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId),
            playerSubstitutionRepository.getMatchSubstitutions(matchId),
            playerRepository.getPlayers(),
        ) { match, playerTimes, substitutions, players ->
            if (match == null) {
                null
            } else {
                val playerTimeSummaries = playerTimes.map { playerTime ->
                    val player = players.find { it.id == playerTime.playerId }
                    PlayerTimeSummary(
                        player = player ?: throw IllegalStateException("Player not found: ${playerTime.playerId}"),
                        elapsedTimeMillis = playerTime.elapsedTimeMillis,
                    )
                }.sortedByDescending { it.elapsedTimeMillis }

                val substitutionSummaries = substitutions.map { substitution ->
                    val playerOut = players.find { it.id == substitution.playerOutId }
                    val playerIn = players.find { it.id == substitution.playerInId }
                    SubstitutionSummary(
                        playerOut = playerOut ?: throw IllegalStateException("Player not found: ${substitution.playerOutId}"),
                        playerIn = playerIn ?: throw IllegalStateException("Player not found: ${substitution.playerInId}"),
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
