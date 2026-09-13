package com.jesuslcorominas.teamflowmanager.data.core.dto

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable representation of a pending substitution. It lives in data/core because the domain
 * model cannot carry @Serializable: domain has no serialization plugin.
 */
@Serializable
internal data class PendingSubstitutionPairDto(
    @SerialName("out") val playerOutId: String,
    @SerialName("in") val playerInId: String,
)

internal fun PendingSubstitutionPairDto.toDomain(): SubstitutionPair = SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)

internal fun SubstitutionPair.toDto(): PendingSubstitutionPairDto = PendingSubstitutionPairDto(playerOutId = playerOutId, playerInId = playerInId)
