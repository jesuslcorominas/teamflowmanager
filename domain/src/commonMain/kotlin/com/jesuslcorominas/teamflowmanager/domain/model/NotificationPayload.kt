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
    }
}
