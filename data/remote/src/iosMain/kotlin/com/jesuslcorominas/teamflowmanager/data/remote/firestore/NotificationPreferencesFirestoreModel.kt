package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferencesFirestoreModel(
    override val matchEvents: Boolean = true,
    override val goals: Boolean = true,
    override val teams: Map<String, TeamPrefsModel> = emptyMap(),
) : NotificationPreferencesFields {
    @Serializable
    data class TeamPrefsModel(
        override val matchEvents: Boolean = true,
        override val goals: Boolean = true,
    ) : TeamPrefsFields
}
