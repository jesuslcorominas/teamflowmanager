package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.ic_launcher

@Composable
fun TeamFlowManagerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 144.dp,
) {
    Icon(
        modifier = modifier.size(size),
        painter = painterResource(Res.drawable.ic_launcher),
        contentDescription = null,
        tint = Color.Unspecified,
    )
}
