package com.jesuslcorominas.teamflowmanager.ui.components

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMElevation

@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String,
    dismissText: String,
    modifier: Modifier = Modifier,
    isCancelEnabled: Boolean = true,
    isConfirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = TFMElevation.level3,
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(TFMSpacing.spacing06),
                verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                content()

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = TFMSpacing.spacing02),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss, enabled = isCancelEnabled) {
                        Text(dismissText)
                    }
                    Spacer(Modifier.width(TFMSpacing.spacing02))
                    Button(onClick = onConfirm, enabled = isConfirmEnabled) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
