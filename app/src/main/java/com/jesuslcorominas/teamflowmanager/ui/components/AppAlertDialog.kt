package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {},
    confirmText: String,
    dismissText: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = icon?.let { { Icon(it, contentDescription = iconContentDescription) } },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors =
                    if (isDestructive) {
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            if (!dismissText.isNullOrBlank()) {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}
