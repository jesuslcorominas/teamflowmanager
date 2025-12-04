package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun TeamFlowManagerIcon() {
    Icon(
        modifier = Modifier.size(TFMSpacing.spacing18),
        painter = painterResource(id = R.drawable.ic_launcher),
        contentDescription = stringResource(R.string.app_name),
        tint = Color.Unspecified
    )
}
