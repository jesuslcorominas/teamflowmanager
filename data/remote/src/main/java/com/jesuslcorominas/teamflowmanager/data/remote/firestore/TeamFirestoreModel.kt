package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType

/**
 * Firestore model for Team document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `assignedCoachId` field stores the user ID of the assigned coach (null if no coach assigned yet).
 * The `clubId` field is optional - teams can be orphaned (null) or belong to a club (string).
 */
data class TeamFirestoreModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val captainId: Long? = null,
    val teamType: Int = TeamType.FOOTBALL_5.players,
    val assignedCoachId: String? = null,
    val clubId: String? = null,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        name = "",
        coachName = "",
        delegateName = "",
        captainId = null,
        teamType = TeamType.FOOTBALL_5.players,
        assignedCoachId = null,
        clubId = null,
    )
}

fun TeamFirestoreModel.toDomain(): Team =
    Team(
        id = id.toStableId(), // Generate a consistent Long id from document id
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = TeamType.fromPlayers(teamType),
        coachId = assignedCoachId, // Store the assigned coach's user ID (null if no coach assigned)
        clubId = clubId?.takeIf { it.isNotEmpty() }?.toStableId(), // Convert club Firestore ID to Long, null if empty/null
        clubFirestoreId = clubId?.takeIf { it.isNotEmpty() }, // Store original club Firestore ID
        firestoreId = id, // Store the Firestore document ID
    )

fun Team.toFirestoreModel(): TeamFirestoreModel =
    TeamFirestoreModel(
        id = firestoreId ?: "", // Use the team's Firestore document ID
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = teamType.players,
        assignedCoachId = coachId, // Store the assigned coach's user ID
        clubId = clubFirestoreId, // Use the stored club Firestore ID, null for orphaned teams
    )
