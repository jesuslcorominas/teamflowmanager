package com.jesuslcorominas.teamflowmanager.data.remote.firestore

interface ClubFields {
    val id: String
    val ownerId: String
    val name: String
    val invitationCode: String
    val homeGround: String?
}

interface PresidentNotificationFields {
    val id: String
    val type: String
    val title: String
    val body: String
    val userData: Map<String, String>
    val createdAt: Long
    val read: Boolean
}

interface TeamPrefsFields {
    val matchEvents: Boolean
    val goals: Boolean
}

interface NotificationPreferencesFields {
    val matchEvents: Boolean
    val goals: Boolean
    val teams: Map<String, out TeamPrefsFields>
}
