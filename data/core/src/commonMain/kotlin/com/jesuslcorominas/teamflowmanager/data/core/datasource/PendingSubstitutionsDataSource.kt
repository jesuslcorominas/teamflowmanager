package com.jesuslcorominas.teamflowmanager.data.core.datasource

/**
 * Storage clave-valor opaco para los pendientes de un partido. No conoce el formato del [String]:
 * la serialización vive en data/core.
 */
interface PendingSubstitutionsDataSource {
    /** Devuelve el payload guardado para [matchId], o `null` si no hay nada. */
    fun getRaw(matchId: String): String?

    /** Guarda [raw] para [matchId]. Un [raw] `null` borra la entrada. */
    fun setRaw(
        matchId: String,
        raw: String?,
    )
}
