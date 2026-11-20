package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.runtime.Composable
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.club_role_coach
import teamflowmanager.shared_ui.generated.resources.club_role_president
import teamflowmanager.shared_ui.generated.resources.club_role_staff

/**
 * Maps a persisted club role name ([ClubRole.roleName]) to its localized string resource.
 * Returns null for unknown roles so callers can fall back to the raw value.
 */
fun clubRoleLabelResource(roleName: String): StringResource? =
    when (ClubRole.fromString(roleName)) {
        ClubRole.PRESIDENT -> Res.string.club_role_president
        ClubRole.COACH -> Res.string.club_role_coach
        ClubRole.STAFF -> Res.string.club_role_staff
        null -> null
    }

@Composable
fun clubRolesLabel(roles: List<String>): String =
    roles.map { role ->
        clubRoleLabelResource(role)?.let { stringResource(it) } ?: role
    }.joinToString(", ")
