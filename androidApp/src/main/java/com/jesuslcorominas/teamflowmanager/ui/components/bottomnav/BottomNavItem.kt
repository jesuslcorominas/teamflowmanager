package com.jesuslcorominas.teamflowmanager.ui.components.bottomnav

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.components.icon.FlipIcon

@Composable
fun BottomNavItem(iconVector: ImageVector, labelResId: Int, isSelected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        val animatedHeight by animateDpAsState(
            targetValue = if (isSelected) 48.dp else 40.dp,
            label = ""
        )
        val animatedAlpha by animateFloatAsState(
            targetValue = if (isSelected) 1f else .5f,
            label = ""
        )
        val animatedIconSize by animateDpAsState(
            targetValue = if (isSelected) 24.dp else 20.dp,
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            ), label = ""
        )
        Column(
            modifier = Modifier
                .height(animatedHeight),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlipIcon(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .alpha(animatedAlpha)
                    .size(animatedIconSize),
                isActive = isSelected,
                activeIcon = iconVector,
                inactiveIcon = iconVector,
                contentDescription = stringResource(id = labelResId)
            )

            if (isSelected) {
                Text(
                    text = stringResource(id = labelResId),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
