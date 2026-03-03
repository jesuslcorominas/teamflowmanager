package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class TitleSize {
    SMALL,
    MEDIUM,
    LARGE
}

@Composable
fun AppTitle(
    modifier: Modifier = Modifier,
    title: String,
    size: TitleSize = TitleSize.MEDIUM,
    color: Color = MaterialTheme.colorScheme.onSurface,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        startIcon?.let {
            Icon(
                modifier = Modifier.size(size.toIconSize()),
                imageVector = startIcon,
                contentDescription = "",
                tint = color,
            )
        }

        Text(
            text = title,
            style = size.toTextStyle(),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
        )

        endIcon?.let {
            Icon(
                imageVector = endIcon,
                modifier = Modifier.size(size.toIconSize()),
                contentDescription = "",
                tint = color,
            )
        }
    }
}

private fun TitleSize.toIconSize() = when (this) {
    TitleSize.LARGE -> 32.dp
    TitleSize.MEDIUM -> 24.dp
    TitleSize.SMALL -> 20.dp
}

@Composable
private fun TitleSize.toTextStyle() = when (this) {
    TitleSize.LARGE -> MaterialTheme.typography.titleLarge
    TitleSize.MEDIUM -> MaterialTheme.typography.titleMedium
    TitleSize.SMALL -> MaterialTheme.typography.titleSmall
}
