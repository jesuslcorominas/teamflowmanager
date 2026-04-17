package com.jesuslcorominas.teamflowmanager.domain.usecase

sealed class MatchEventNotification {
    data class Start(val teamName: String, val opponent: String) : MatchEventNotification()

    data class End(val teamName: String, val opponent: String, val teamGoals: Int, val opponentGoals: Int) : MatchEventNotification()

    data class Goal(val teamName: String, val teamGoals: Int, val opponentGoals: Int, val minuteOfPlay: Int?) : MatchEventNotification()
}

interface NotifyPresidentMatchEventUseCase {
    suspend operator fun invoke(
        event: MatchEventNotification,
        teamRemoteId: String,
        clubRemoteId: String,
    )
}
