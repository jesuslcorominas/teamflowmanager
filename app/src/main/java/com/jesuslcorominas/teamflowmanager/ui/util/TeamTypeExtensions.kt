package com.jesuslcorominas.teamflowmanager.ui.util

import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType

fun TeamType.toStringRes() = when (this) {
    TeamType.FOOTBALL_5 -> R.string.team_type_football_5
    TeamType.FOOTBALL_7 -> R.string.team_type_football_7
    TeamType.FOOTBALL_8 -> R.string.team_type_football_8
    TeamType.FOOTBALL_11 -> R.string.team_type_football_11
}
