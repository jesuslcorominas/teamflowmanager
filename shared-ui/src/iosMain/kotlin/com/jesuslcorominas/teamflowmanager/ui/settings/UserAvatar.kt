package com.jesuslcorominas.teamflowmanager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
actual fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
