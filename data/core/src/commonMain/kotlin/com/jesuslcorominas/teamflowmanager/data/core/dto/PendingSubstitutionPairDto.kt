package com.jesuslcorominas.teamflowmanager.data.core.dto

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representación serializable de una sustitución pendiente. Vive en data/core porque el modelo de
 * domain no puede llevar @Serializable (domain no tiene el plugin de serialización).
 */
@Serializable
internal data class PendingSubstitutionPairDto(
    @SerialName("out") val playerOutId: String,
    @SerialName("in") val playerInId: String,
)

internal fun PendingSubstitutionPairDto.toDomain(): SubstitutionPair = SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)

internal fun SubstitutionPair.toDto(): PendingSubstitutionPairDto = PendingSubstitutionPairDto(playerOutId = playerOutId, playerInId = playerInId)
