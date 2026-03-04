package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club

// ── Stub datasources for iOS Phase 2 MVP ─────────────────────────────────────
// Write operations and local-only operations throw NotImplementedError.
// Read operations return safe empty/null defaults so the app navigates normally
// when no data is present.

class ClubFirestoreDataSourceImpl : ClubDataSource {
    override suspend fun createClubWithOwner(
        clubName: String, currentUserId: String,
        currentUserName: String, currentUserEmail: String
    ): Club = throw NotImplementedError("createClubWithOwner not implemented for iOS Phase 2")

    override suspend fun getClubByInvitationCode(invitationCode: String): Club? = null
}



class NoOpImageStorageDataSource : ImageStorageDataSource {
    override suspend fun uploadImage(localUri: String, path: String): String? = null
    override suspend fun deleteImage(downloadUrl: String): Boolean = false
}

class NoOpDynamicLinkDataSource : DynamicLinkDataSource {
    override suspend fun generateTeamInvitationLink(teamFirestoreId: String, teamName: String): String =
        throw NotImplementedError("generateTeamInvitationLink not implemented for iOS Phase 2")
}
