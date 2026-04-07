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

        data object UserWaitingForAssignment : Typed() {
            override val type: NotificationType = NotificationType.USER_WAITING_FOR_ASSIGNMENT
            override val params: Map<String, String> = emptyMap()
        }
    }
}
