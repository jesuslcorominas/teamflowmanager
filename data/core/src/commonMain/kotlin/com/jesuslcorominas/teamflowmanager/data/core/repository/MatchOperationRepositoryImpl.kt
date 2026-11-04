package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import kotlinx.datetime.Clock
import kotlin.coroutines.cancellation.CancellationException

internal class MatchOperationRepositoryImpl(
    private val matchOperationDataSource: MatchOperationDataSource,
    // Injected so tests can supply a fixed timestamp instead of relying on the real wall clock.
    private val currentTimeProvider: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : MatchOperationRepository {
    override suspend fun createOperation(operation: MatchOperation): String = matchOperationDataSource.createOperation(operation)

    override suspend fun updateOperation(operation: MatchOperation) {
        matchOperationDataSource.updateOperation(operation)
    }

    override suspend fun getOperationById(operationId: String): MatchOperation? = matchOperationDataSource.getOperationById(operationId)

    override suspend fun commitOperation(commit: MatchOperationCommit) {
        matchOperationDataSource.commitOperation(commit)
    }

    /**
     * Fetches all IN_PROGRESS operations for [matchId] from the datasource, filters locally by
     * [olderThanMillis] (single source of truth for the age criterion), and marks each stale
     * operation as FAILED.
     *
     * Filtering is done here in data/core so it is testable with MockK without a Firestore emulator.
     * Individual failures are swallowed so a flaky connection on one operation does not abort the
     * entire sweep — the next session opening will retry any that remain IN_PROGRESS.
     */
    override suspend fun markStaleOperationsAsFailed(
        matchId: String,
        olderThanMillis: Long,
    ) {
        val cutoffMillis = currentTimeProvider() - olderThanMillis
        val staleOperations =
            matchOperationDataSource
                .getOperationsByMatchAndStatus(matchId, MatchOperationStatus.IN_PROGRESS)
                .filter { it.createdAt < cutoffMillis }

        staleOperations.forEach { operation ->
            try {
                matchOperationDataSource.updateOperation(operation.copy(status = MatchOperationStatus.FAILED))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Continue sweeping remaining operations rather than aborting the whole batch.
                // Logged so a systematic failure does not leave operations stuck IN_PROGRESS silently.
                println("MatchOperationRepository: could not mark operation ${operation.id} as FAILED: ${e.message}")
            }
        }
    }
}
