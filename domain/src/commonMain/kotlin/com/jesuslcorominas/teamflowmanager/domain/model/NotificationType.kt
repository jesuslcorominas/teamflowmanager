package com.jesuslcorominas.teamflowmanager.domain.model

enum class NotificationType(val key: String) {
    ASSIGNED_AS_COACH("ASSIGNED_AS_COACH"),
    USER_WAITING_FOR_ASSIGNMENT("USER_WAITING_FOR_ASSIGNMENT"),
    MATCH_START("MATCH_START"),
    MATCH_END("MATCH_END"),
    GOAL("GOAL"),
}
