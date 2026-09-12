package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

/**
 * Records goals — the team's, an own goal, and the opponent's — and fires the president's
 * notification for each.
 *
 * Knows nothing about dialogs: whether a sheet is open is screen state, and it stays with the
 * ViewModel. Both methods throw on failure after reporting, which is what keeps the dialog open
 * when something went wrong.
 */
internal class MatchGoalRecorder(
    private val registerGoal: RegisterGoalUseCase,
    private val getMatchById: GetMatchByIdUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) {
    suspend fun record(
        scope: CoroutineScope,
        notifications: MatchNotificationCoordinator,
        matchId: String,
        scorerId: String?,
        currentTimeMillis: Long,
        team: Team?,
    ) {
        try {
            crashReporter.log(
                "Registering ${scorerId?.let { "goal for player: $it" } ?: "own goal (autogol by rival)"}",
            )
            registerGoal(
                matchId = matchId,
                scorerId = scorerId,
                currentTimeMillis = currentTimeMillis,
                isOpponentGoal = false,
                isOwnGoal = scorerId == null,
            )

            analyticsTracker.logEvent(
                AnalyticsEvent.GOAL_SCORED,
                mapOf(
                    AnalyticsParam.MATCH_ID to matchId,
                    AnalyticsParam.PLAYER_ID to (scorerId ?: ""),
                    AnalyticsParam.GOAL_MINUTE to (currentTimeMillis / 60000).toString(),
                    AnalyticsParam.TEAM_TYPE to (scorerId?.let { "own" } ?: "own_goal"),
                ).filter { it.value.isNotBlank() },
            )

            notify(scope, notifications, matchId, currentTimeMillis, team, isOpponentGoal = false)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            crashReporter.log("Error registering goal: ${e.message}")
            throw e
        }
    }

    suspend fun recordOpponent(
        scope: CoroutineScope,
        notifications: MatchNotificationCoordinator,
        matchId: String,
        currentTimeMillis: Long,
        team: Team?,
    ) {
        try {
            crashReporter.log("Registering opponent goal")
            // For opponent goals, scorerId is null since opponent players are not tracked
            registerGoal(
                matchId = matchId,
                scorerId = null,
                currentTimeMillis = currentTimeMillis,
                isOpponentGoal = true,
            )

            analyticsTracker.logEvent(
                AnalyticsEvent.OPPONENT_GOAL_SCORED,
                mapOf(
                    AnalyticsParam.MATCH_ID to matchId,
                    AnalyticsParam.GOAL_MINUTE to (currentTimeMillis / 60000).toString(),
                    AnalyticsParam.TEAM_TYPE to "opponent",
                ),
            )

            notify(scope, notifications, matchId, currentTimeMillis, team, isOpponentGoal = true)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            crashReporter.log("Error registering opponent goal: ${e.message}")
            throw e
        }
    }

    /** Re-reads the match so the notification carries the score as it stands after the goal. */
    private fun notify(
        scope: CoroutineScope,
        notifications: MatchNotificationCoordinator,
        matchId: String,
        snapshotTime: Long,
        team: Team?,
        isOpponentGoal: Boolean,
    ) {
        notifications.fireNotification(
            scope = scope,
            team = team,
            matchId = matchId,
        ) {
            val updatedMatch = getMatchById(matchId).first() ?: return@fireNotification null
            MatchEventNotification.Goal(
                teamName = updatedMatch.teamName,
                opponentName = updatedMatch.opponent,
                teamGoals = updatedMatch.goals,
                opponentGoals = updatedMatch.opponentGoals,
                minuteOfPlay = notifications.minuteOfPlay(updatedMatch, snapshotTime),
                isOpponentGoal = isOpponentGoal,
            )
        }
    }
}
