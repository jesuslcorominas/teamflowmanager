package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Position

fun Position.toLocalizedString(context: Context): String =
    when (this) {
        Position.Goalkeeper -> context.getString(R.string.position_goalkeeper)
        Position.Defender -> context.getString(R.string.position_defender)
        Position.RightBack -> context.getString(R.string.position_right_back)
        Position.LeftBack -> context.getString(R.string.position_left_back)
        Position.CenterBack -> context.getString(R.string.position_center_back)
        Position.Midfielder -> context.getString(R.string.position_midfielder)
        Position.DefensiveMidfielder -> context.getString(R.string.position_defensive_midfielder)
        Position.CentralMidfielder -> context.getString(R.string.position_central_midfielder)
        Position.AttackingMidfielder -> context.getString(R.string.position_attacking_midfielder)
        Position.Forward -> context.getString(R.string.position_forward)
        Position.Winger -> context.getString(R.string.position_winger)
        Position.Striker -> context.getString(R.string.position_striker)
    }
