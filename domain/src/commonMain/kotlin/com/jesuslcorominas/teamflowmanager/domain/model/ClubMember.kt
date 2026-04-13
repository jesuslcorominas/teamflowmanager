package com.jesuslcorominas.teamflowmanager.domain.model

data class ClubMember(
    val id: Long,
    val userId: String,
    val name: String,
    val email: String,
    val clubId: Long,
    val roles: List<String>,
    val remoteId: String? = null,
    val clubRemoteId: String? = null,
) {
    /**
     * Helper function to check if the member has a specific role.
     */
    fun hasRole(role: String): Boolean = roles.contains(role)

    /**
     * Helper function to check if the member has a specific ClubRole.
     */
    fun hasRole(clubRole: ClubRole): Boolean = roles.contains(clubRole.roleName)
}
