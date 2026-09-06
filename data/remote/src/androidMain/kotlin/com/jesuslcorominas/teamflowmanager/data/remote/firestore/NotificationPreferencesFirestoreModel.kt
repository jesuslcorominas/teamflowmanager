package com.jesuslcorominas.teamflowmanager.data.remote.firestore

data class NotificationPreferencesFirestoreModel(
    override val matchEvents: Boolean = true,
    override val goals: Boolean = true,
    override val teams: Map<String, TeamPrefsModel> = emptyMap(),
) : NotificationPreferencesFields {
    data class TeamPrefsModel(
        override val matchEvents: Boolean = true,
        override val goals: Boolean = true,
    ) : TeamPrefsFields
}
