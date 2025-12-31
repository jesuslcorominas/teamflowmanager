package com.jesuslcorominas.teamflowmanager.domain.model

enum class ClubRole(val roleName: String) {
    PRESIDENT("Presidente"),
    COACH("Coach"),
    MEMBER("Member");

    companion object {
        fun fromString(role: String): ClubRole? {
            return entries.find { it.roleName.equals(role, ignoreCase = true) }
        }
    }
}
