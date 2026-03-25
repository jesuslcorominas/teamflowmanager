package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * iOS placeholder for the app icon.
 * TODO: replace with proper CMP drawable resource once the icon is exported
 * to SVG/PNG and added to shared-ui/commonMain/composeResources/drawable/.
 */
@Composable
actual fun TeamFlowManagerIcon() {
    Box(
        modifier =
            Modifier
                .size(144.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(28.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "TFM",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}
