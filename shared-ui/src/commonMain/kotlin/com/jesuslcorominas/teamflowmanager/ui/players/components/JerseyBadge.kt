package com.jesuslcorominas.teamflowmanager.ui.players.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.BebasNeueFontFamily
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.ShirtOrange
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.theme.White

@Composable
fun JerseyBadge(
    number: Int,
    modifier: Modifier = Modifier,
    size: Int = 56,
    cornerShape: RoundedCornerShape = RoundedCornerShape(8.dp),
) {
    Box(
        modifier =
            modifier
                .clip(cornerShape)
                .background(BackgroundContrast)
                .size(size.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(top = TFMSpacing.spacing02),
                text = number.toString(),
                fontFamily = BebasNeueFontFamily,
                color = ContentContrast,
                style = if (size >= 56) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
            )
        }

        Column {
            Spacer(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(TFMSpacing.spacing02)
                        .background(ShirtOrange),
            )
            Spacer(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(TFMSpacing.spacing01)
                        .background(White),
            )
        }
    }
}
