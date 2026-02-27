package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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

class PlayerFirestoreDataSourceImpl : PlayerDataSource {
    override fun getAllPlayers(): Flow<List<Player>> = flowOf(emptyList())
    override suspend fun getPlayerById(playerId: Long): Player? = null
    override suspend fun getCaptainPlayer(): Player? = null
    override suspend fun setPlayerAsCaptain(playerId: Long) =
        throw NotImplementedError("setPlayerAsCaptain not implemented for iOS Phase 2")
    override suspend fun removePlayerAsCaptain(playerId: Long) =
        throw NotImplementedError("removePlayerAsCaptain not implemented for iOS Phase 2")
    override suspend fun updatePlayer(player: Player) =
        throw NotImplementedError("updatePlayer not implemented for iOS Phase 2")
    override suspend fun insertPlayer(player: Player): Long =
        throw NotImplementedError("insertPlayer not implemented for iOS Phase 2")
    override suspend fun deletePlayer(playerId: Long) =
        throw NotImplementedError("deletePlayer not implemented for iOS Phase 2")
    override suspend fun getAllPlayersDirect(): List<Player> = emptyList()
    override suspend fun clearLocalData() = Unit
}

class GoalFirestoreDataSourceImpl : GoalDataSource {
    override fun getMatchGoals(matchId: Long): Flow<List<Goal>> = flowOf(emptyList())
    override fun getAllTeamGoals(): Flow<List<Goal>> = flowOf(emptyList())
    override suspend fun insertGoal(goal: Goal): Long =
        throw NotImplementedError("insertGoal not implemented for iOS Phase 2")
    override suspend fun getAllGoalsDirect(): List<Goal> = emptyList()
    override suspend fun clearLocalData() = Unit
}

class PlayerSubstitutionFirestoreDataSourceImpl : PlayerSubstitutionDataSource {
    override fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>> = flowOf(emptyList())
    override suspend fun insertSubstitution(substitution: PlayerSubstitution): Long =
        throw NotImplementedError("insertSubstitution not implemented for iOS Phase 2")
    override suspend fun getAllPlayerSubstitutionsDirect(): List<PlayerSubstitution> = emptyList()
    override suspend fun clearLocalData() = Unit
}

class PlayerTimeFirestoreDataSourceImpl : PlayerTimeDataSource {
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> = flowOf(null)
    override fun getAllPlayerTimes(): Flow<List<PlayerTime>> = flowOf(emptyList())
    override suspend fun upsertPlayerTime(playerTime: PlayerTime) =
        throw NotImplementedError("upsertPlayerTime not implemented for iOS Phase 2")
    override suspend fun batchUpsertPlayerTimes(playerTimes: List<PlayerTime>) =
        throw NotImplementedError("batchUpsertPlayerTimes not implemented for iOS Phase 2")
    override suspend fun deleteAllPlayerTimes() =
        throw NotImplementedError("deleteAllPlayerTimes not implemented for iOS Phase 2")
    override suspend fun getAllPlayerTimesDirect(): List<PlayerTime> = emptyList()
    override suspend fun clearLocalData() = Unit
}

class PlayerTimeHistoryFirestoreDataSourceImpl : PlayerTimeHistoryDataSource {
    override fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>> = flowOf(emptyList())
    override fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>> = flowOf(emptyList())
    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> = flowOf(emptyList())
    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long =
        throw NotImplementedError("insertPlayerTimeHistory not implemented for iOS Phase 2")
    override suspend fun getAllPlayerTimeHistoryDirect(): List<PlayerTimeHistory> = emptyList()
    override suspend fun clearLocalData() = Unit
}

class MatchOperationFirestoreDataSourceImpl : MatchOperationDataSource {
    override suspend fun createOperation(operation: MatchOperation): String =
        throw NotImplementedError("createOperation not implemented for iOS Phase 2")
    override suspend fun updateOperation(operation: MatchOperation) =
        throw NotImplementedError("updateOperation not implemented for iOS Phase 2")
    override suspend fun getOperationById(operationId: String): MatchOperation? = null
}

class NoOpImageStorageDataSource : ImageStorageDataSource {
    override suspend fun uploadImage(localUri: String, path: String): String? = null
    override suspend fun deleteImage(downloadUrl: String): Boolean = false
}

class NoOpDynamicLinkDataSource : DynamicLinkDataSource {
    override suspend fun generateTeamInvitationLink(teamFirestoreId: String, teamName: String): String =
        throw NotImplementedError("generateTeamInvitationLink not implemented for iOS Phase 2")
}
