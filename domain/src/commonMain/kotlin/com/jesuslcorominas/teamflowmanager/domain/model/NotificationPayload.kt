package com.jesuslcorominas.teamflowmanager.domain.model

sealed class NotificationPayload {
    data class FreeText(
        val title: String,
        val body: String,
    ) : NotificationPayload()

    sealed class Typed : NotificationPayload() {
        abstract val type: NotificationType
        abstract val params: Map<String, String>

        data class AssignedAsCoach(val teamName: String) : Typed() {
            override val type: NotificationType = NotificationType.ASSIGNED_AS_COACH
            override val params: Map<String, String> = mapOf("teamName" to teamName)
        }

        data class UserWaitingForAssignment(
            val userName: String,
            val userEmail: String,
        ) : Typed() {
            override val type: NotificationType = NotificationType.USER_WAITING_FOR_ASSIGNMENT
            override val params: Map<String, String> =
                mapOf(
                    "userName" to userName,
                    "userEmail" to userEmail,
                )
        }

        data class MatchStart(val teamName: String, val opponent: String) : Typed() {
            override val type = NotificationType.MATCH_START
            override val params = mapOf("teamName" to teamName, "opponent" to opponent)
        }

        data class MatchEnd(
            val teamName: String,
            val opponent: String,
            val teamGoals: Int,
            val opponentGoals: Int,
        ) : Typed() {
            override val type = NotificationType.MATCH_END
            override val params =
                mapOf(
                    "teamName" to teamName,
                    "opponent" to opponent,
                    "teamGoals" to teamGoals.toString(),
                    "opponentGoals" to opponentGoals.toString(),
                )
        }

        data class GoalScored(
            val teamName: String,
            val opponentName: String,
            val teamGoals: Int,
            val opponentGoals: Int,
            val minuteOfPlay: String?,
            val isOpponentGoal: Boolean,
        ) : Typed() {
            override val type = NotificationType.GOAL
            override val params =
                buildMap {
                    put("teamName", teamName)
                    put("opponentName", opponentName)
                    put("teamGoals", teamGoals.toString())
                    put("opponentGoals", opponentGoals.toString())
                    put("isOpponentGoal", isOpponentGoal.toString())
                    minuteOfPlay?.let { put("minuteOfPlay", it) }
                }
        }
    }
}
