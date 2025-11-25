package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp


@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    internalModifier: Modifier = Modifier.size(48.dp),
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    tint: Color?,
    onClick: () -> Unit
) {
    IconButton(
        modifier = if (modifier == Modifier) Modifier.size(64.dp) else modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            modifier = internalModifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint
                ?.let { tint }
                ?: if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    }
}

@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    internalModifier: Modifier = Modifier.size(48.dp),
    imageVector: ImageVector,
    @StringRes contentDescription: Int,
    enabled: Boolean = true,
    tint: Color?,
    onClick: () -> Unit
) {
    AppIconButton(
        modifier = modifier,
        internalModifier = internalModifier,
        imageVector = imageVector,
        contentDescription = stringResource(contentDescription),
        enabled = enabled,
        tint = tint,
        onClick = onClick
    )
}

@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    internalModifier: Modifier = Modifier.size(48.dp),
    @DrawableRes icon: Int,
    @StringRes contentDescription: Int,
    enabled: Boolean = true,
    tint: Color?,
    onClick: () -> Unit
) {
    AppIconButton(
        modifier = modifier,
        internalModifier = internalModifier,
        imageVector = ImageVector.vectorResource(icon),
        contentDescription = contentDescription,
        enabled = enabled,
        tint = tint,
        onClick = onClick
    )
}
