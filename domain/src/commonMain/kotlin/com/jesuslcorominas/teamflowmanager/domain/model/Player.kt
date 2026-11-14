package com.jesuslcorominas.teamflowmanager.domain.model

data class Player(
    val id: String,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: List<Position>,
    val teamId: String,
    val isCaptain: Boolean,
    val imageUri: String? = null,
    val deleted: Boolean = false,
) {
    /**
     * Name to show in the UI.
     *
     * The surname is optional, so it is left out when blank instead of leaving a trailing
     * space. This is the single place the two parts get joined: formatting them at each
     * call site is how the trailing spaces appeared in the first place.
     */
    val displayName: String
        get() =
            listOf(firstName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.trim() }
}
