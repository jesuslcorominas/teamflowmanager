package com.jesuslcorominas.teamflowmanager.data.local.entity

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution

data class PlayerSubstitutionEntity(
    val id: Long = 0L,
    val matchId: Long,
    val playerOutId: Long,
    val playerInId: Long,
    val substitutionTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
)

fun PlayerSubstitutionEntity.toDomain(): PlayerSubstitution =
    PlayerSubstitution(
        id = id,
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )

fun PlayerSubstitution.toEntity(): PlayerSubstitutionEntity =
    PlayerSubstitutionEntity(
        id = id,
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )
