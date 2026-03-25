package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Migration step information
 */
data class MigrationStep(
    val step: Int,
    val totalSteps: Int,
    val description: String,
)
