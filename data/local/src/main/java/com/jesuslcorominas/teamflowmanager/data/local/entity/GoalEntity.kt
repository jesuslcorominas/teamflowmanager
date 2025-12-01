package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Goal

@Entity(
    tableName = "goal",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("matchId")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val matchId: Long,
    val scorerId: Long?,
    val goalTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val isOpponentGoal: Boolean = false
)

fun GoalEntity.toDomain(): Goal =
    Goal(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
        isOwnGoal = scorerId == null && !isOpponentGoal,
    )

fun Goal.toEntity(): GoalEntity =
    GoalEntity(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
    )
