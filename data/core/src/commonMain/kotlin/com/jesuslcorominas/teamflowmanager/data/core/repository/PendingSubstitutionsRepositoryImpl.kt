package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingSubstitutionsDataSource
import com.jesuslcorominas.teamflowmanager.data.core.dto.PendingSubstitutionPairDto
import com.jesuslcorominas.teamflowmanager.data.core.dto.toDomain
import com.jesuslcorominas.teamflowmanager.data.core.dto.toDto
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class PendingSubstitutionsRepositoryImpl(
    private val pendingSubstitutionsDataSource: PendingSubstitutionsDataSource,
) : PendingSubstitutionsRepository {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * One flow per match. Key-value storage does not notify changes, so the repository keeps the
     * state in memory and updates it on every write. Seeded lazily: a match's flow is created the
     * first time it is asked for, reading what was persisted, so the first emission is the stored
     * value rather than a default.
     *
     * Neither this map nor the read-modify-write sequences below are guarded. That is the threading
     * contract documented on [PendingSubstitutionsRepository]: mutating calls come from a single
     * thread, the one owning the match screen. Guarding it would mean a suspending API, which the
     * rest of the key-value stores in the project do not have.
     */
    private val pendingByMatch = mutableMapOf<String, MutableStateFlow<List<SubstitutionPair>>>()

    override fun observe(matchId: String): Flow<List<SubstitutionPair>> = flowFor(matchId).asStateFlow()

    override fun conflictsFor(
        matchId: String,
        pair: SubstitutionPair,
    ): List<SubstitutionPair> {
        if (pair.isDegenerate()) return emptyList()
        val current = flowFor(matchId).value
        if (current.contains(pair)) return emptyList()
        return current.filter { it.sharesAnyPlayerWith(pair) }
    }

    override fun add(
        matchId: String,
        pair: SubstitutionPair,
    ) {
        // A player substituted for themselves is not a substitution, whatever the caller meant.
        if (pair.isDegenerate()) return
        val current = flowFor(matchId).value
        // Re-adding an identical pair is a no-op: it does not duplicate and does NOT move it last.
        if (current.contains(pair)) return
        persist(matchId, current.filterNot { it.sharesAnyPlayerWith(pair) } + pair)
    }

    override fun remove(
        matchId: String,
        pair: SubstitutionPair,
    ) {
        val current = flowFor(matchId).value
        if (!current.contains(pair)) return
        persist(matchId, current - pair)
    }

    override fun clear(matchId: String) {
        // No early return on an already-empty list: an unreadable payload also reads as empty, and
        // skipping the write would leave it in storage forever.
        persist(matchId, emptyList())
    }

    private fun flowFor(matchId: String): MutableStateFlow<List<SubstitutionPair>> =
        pendingByMatch.getOrPut(matchId) { MutableStateFlow(readFromStorage(matchId)) }

    private fun readFromStorage(matchId: String): List<SubstitutionPair> {
        val raw = pendingSubstitutionsDataSource.getRaw(matchId) ?: return emptyList()
        return try {
            json.decodeFromString<List<PendingSubstitutionPairDto>>(raw).map { it.toDomain() }
        } catch (_: IllegalArgumentException) {
            // SerializationException extends IllegalArgumentException, and decodeFromString also
            // throws IllegalArgumentException on syntactically invalid JSON: unreadable pendings
            // degrade to "this match has none" instead of breaking the app. Logged rather than
            // swallowed, because it means the coach silently lost scheduled cards.
            println("PendingSubstitutionsRepository: unreadable pendings for match $matchId, discarding them")
            emptyList()
        }
    }

    private fun persist(
        matchId: String,
        pairs: List<SubstitutionPair>,
    ) {
        val raw = if (pairs.isEmpty()) null else json.encodeToString(pairs.map { it.toDto() })
        pendingSubstitutionsDataSource.setRaw(matchId, raw)
        flowFor(matchId).value = pairs
    }

    private fun SubstitutionPair.isDegenerate(): Boolean = playerOutId == playerInId

    private fun SubstitutionPair.sharesAnyPlayerWith(other: SubstitutionPair): Boolean =
        playerOutId == other.playerOutId ||
            playerOutId == other.playerInId ||
            playerInId == other.playerOutId ||
            playerInId == other.playerInId
}
