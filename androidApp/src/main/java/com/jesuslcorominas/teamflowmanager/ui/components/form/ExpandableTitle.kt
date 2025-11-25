package com.jesuslcorominas.teamflowmanager.ui.components.form

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun ExpandableTitle(title: String, expanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevronRotation"
    )

    Column {
        Row (
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = TFMSpacing.spacing04),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTitle(modifier = Modifier.weight(1F), title = title)

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.collapse)
                } else {
                    stringResource(R.string.expand)
                },
                modifier = Modifier.graphicsLayer { rotationZ = rotation },
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
