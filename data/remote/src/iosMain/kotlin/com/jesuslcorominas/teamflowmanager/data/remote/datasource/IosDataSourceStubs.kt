package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource

// ── Stub datasources for iOS Phase 2 MVP ─────────────────────────────────────
// Write operations and local-only operations throw NotImplementedError.
// Read operations return safe empty/null defaults so the app navigates normally
// when no data is present.

class NoOpImageStorageDataSource : ImageStorageDataSource {
    override suspend fun uploadImage(
        localUri: String,
        path: String,
    ): String? = null

    override suspend fun deleteImage(downloadUrl: String): Boolean = false
}

class NoOpDynamicLinkDataSource : DynamicLinkDataSource {
    override suspend fun generateTeamInvitationLink(
        teamId: String,
        teamName: String,
    ): String = throw NotImplementedError("generateTeamInvitationLink not implemented for iOS Phase 2")
}
