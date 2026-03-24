package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.sharedui.R

@Composable
actual fun TeamFlowManagerIcon() {
    Icon(
        modifier = Modifier.size(144.dp),
        painter = painterResource(id = R.drawable.ic_launcher),
        contentDescription = null,
        tint = Color.Unspecified,
    )
}
