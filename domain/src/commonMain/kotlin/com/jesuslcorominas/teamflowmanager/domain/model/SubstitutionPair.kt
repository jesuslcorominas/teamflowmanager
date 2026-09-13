package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Una sustitución: un jugador sale, otro entra. Emparejar salida y entrada en el tipo evita
 * que puedan desemparejarse al programar varios cambios y ejecutarlos en lote.
 */
data class SubstitutionPair(
    val playerOutId: String,
    val playerInId: String,
)
