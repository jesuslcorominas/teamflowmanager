package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.coroutines.flow.Flow

/**
 * Almacén local de las sustituciones que el entrenador deja programadas antes de ejecutarlas.
 * Los pendientes son locales al dispositivo, se atan a un [matchId] y sobreviven al cierre de la app.
 * No se sincronizan con Firestore.
 *
 * Invariantes garantizados por la implementación:
 * - Cada pareja empareja exactamente un saliente con un entrante.
 * - Un mismo jugador no aparece en dos parejas a la vez, ni como saliente ni como entrante.
 * - No hay parejas exactamente duplicadas.
 * - Se conserva el orden de inserción.
 */
interface PendingSubstitutionsRepository {
    /**
     * Emite la lista actual de pendientes de [matchId] y cada cambio posterior.
     * Para un partido sin nada guardado emite lista vacía.
     */
    fun observe(matchId: String): Flow<List<SubstitutionPair>>

    /**
     * Añade [pair] al final de la lista de [matchId]. Descarta cualquier pareja previa que comparta
     * el saliente o el entrante de [pair]. Si [pair] ya está exactamente igual, no hace nada.
     */
    fun add(
        matchId: String,
        pair: SubstitutionPair,
    )

    /** Elimina exactamente [pair] de [matchId]. Si no está, no hace nada. */
    fun remove(
        matchId: String,
        pair: SubstitutionPair,
    )

    /** Elimina todos los pendientes de [matchId]. No afecta a otros partidos. */
    fun clear(matchId: String)
}
