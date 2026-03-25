package com.jesuslcorominas.teamflowmanager.ui.util

import androidx.compose.runtime.Composable
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.position_attacking_midfielder
import teamflowmanager.shared_ui.generated.resources.position_center_back
import teamflowmanager.shared_ui.generated.resources.position_central_midfielder
import teamflowmanager.shared_ui.generated.resources.position_defender
import teamflowmanager.shared_ui.generated.resources.position_defensive_midfielder
import teamflowmanager.shared_ui.generated.resources.position_forward
import teamflowmanager.shared_ui.generated.resources.position_goalkeeper
import teamflowmanager.shared_ui.generated.resources.position_left_back
import teamflowmanager.shared_ui.generated.resources.position_midfielder
import teamflowmanager.shared_ui.generated.resources.position_right_back
import teamflowmanager.shared_ui.generated.resources.position_striker
import teamflowmanager.shared_ui.generated.resources.position_winger

@Composable
fun Position.localizedName(): String =
    stringResource(
        when (this) {
            Position.Goalkeeper -> Res.string.position_goalkeeper
            Position.Defender -> Res.string.position_defender
            Position.RightBack -> Res.string.position_right_back
            Position.LeftBack -> Res.string.position_left_back
            Position.CenterBack -> Res.string.position_center_back
            Position.Midfielder -> Res.string.position_midfielder
            Position.DefensiveMidfielder -> Res.string.position_defensive_midfielder
            Position.CentralMidfielder -> Res.string.position_central_midfielder
            Position.AttackingMidfielder -> Res.string.position_attacking_midfielder
            Position.Forward -> Res.string.position_forward
            Position.Winger -> Res.string.position_winger
            Position.Striker -> Res.string.position_striker
        },
    )
