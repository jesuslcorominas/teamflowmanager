package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable

/**
 * Returns an action that opens the platform share sheet with a plain text.
 *
 * On iOS it presents `UIActivityViewController`. On Android it is a no-op: the Android shell
 * passes its own share callback (an `ACTION_SEND` chooser) to the screens that share text.
 */
@Composable
expect fun rememberShareTextAction(): (String) -> Unit
