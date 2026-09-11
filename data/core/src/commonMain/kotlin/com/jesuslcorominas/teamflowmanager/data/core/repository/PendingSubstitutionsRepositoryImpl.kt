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
     * Un flow por partido. El storage clave-valor no notifica cambios, así que el repositorio mantiene
     * el estado en memoria y lo actualiza en cada escritura. Siembra perezosa: el flow de un matchId se
     * crea la primera vez que se pide, leyendo lo persistido, para que la primera emisión sea el valor
     * guardado y no un default.
     *
     * Acceso confinado al hilo principal (ViewModel/UI), igual que PreferencesRepositoryImpl: no hay lock.
     */
    private val pendingByMatch = mutableMapOf<String, MutableStateFlow<List<SubstitutionPair>>>()

    override fun observe(matchId: String): Flow<List<SubstitutionPair>> = flowFor(matchId).asStateFlow()

    override fun add(
        matchId: String,
        pair: SubstitutionPair,
    ) {
        val current = flowFor(matchId).value
        // Re-añadir una pareja idéntica es un no-op: no duplica y NO la mueve al final.
        if (current.contains(pair)) return
        val withoutConflicts = current.filterNot { it.sharesAnyPlayerWith(pair) }
        persist(matchId, withoutConflicts + pair)
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
        if (flowFor(matchId).value.isEmpty()) return
        persist(matchId, emptyList())
    }

    private fun flowFor(matchId: String): MutableStateFlow<List<SubstitutionPair>> =
        pendingByMatch.getOrPut(matchId) { MutableStateFlow(readFromStorage(matchId)) }

    private fun readFromStorage(matchId: String): List<SubstitutionPair> {
        val raw = pendingSubstitutionsDataSource.getRaw(matchId) ?: return emptyList()
        return try {
            json.decodeFromString<List<PendingSubstitutionPairDto>>(raw).map { it.toDomain() }
        } catch (_: IllegalArgumentException) {
            // SerializationException extiende IllegalArgumentException, y decodeFromString también lanza
            // IllegalArgumentException con JSON sintácticamente inválido: un JSON corrupto se degrada a
            // "este partido no tiene pendientes" en lugar de romper la app.
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

    private fun SubstitutionPair.sharesAnyPlayerWith(other: SubstitutionPair): Boolean =
        playerOutId == other.playerOutId ||
            playerOutId == other.playerInId ||
            playerInId == other.playerOutId ||
            playerInId == other.playerInId
}
