package com.jesuslcorominas.teamflowmanager.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.jesuslcorominas.teamflowmanager.shared.ui.App

/**
 * Creates a UIViewController for the Compose UI that can be used in iOS.
 */
fun MainViewController() = ComposeUIViewController { App() }
