package com.jesuslcorominas.teamflowmanager

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AuthenticationServices.ASAuthorizationAppleIDButton
import platform.AuthenticationServices.ASAuthorizationAppleIDButtonStyle
import platform.AuthenticationServices.ASAuthorizationAppleIDButtonTypeSignIn
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControlEventTouchUpInside
import platform.darwin.NSObject

/**
 * Apple's own Sign in with Apple button (ASAuthorizationAppleIDButton), so the
 * look, the logo and the localized title are exactly what App Review expects.
 * Black style: the app always renders with the light colour scheme.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun AppleSignInButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val currentOnClick = rememberUpdatedState(onClick)
    val target = remember { ButtonTarget { currentOnClick.value() } }
    UIKitView(
        factory = {
            ASAuthorizationAppleIDButton(
                authorizationButtonType = ASAuthorizationAppleIDButtonTypeSignIn,
                authorizationButtonStyle = ASAuthorizationAppleIDButtonStyle.ASAuthorizationAppleIDButtonStyleBlack,
            ).apply {
                // Same pill shape as the Google button above it.
                cornerRadius = 24.0
                addTarget(target, NSSelectorFromString("onTap"), UIControlEventTouchUpInside)
            }
        },
        update = { button ->
            button.enabled = enabled
            button.alpha = if (enabled) 1.0 else 0.5
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp),
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true),
    )
}

private class ButtonTarget(
    private val action: () -> Unit,
) : NSObject() {
    // Wired by name through NSSelectorFromString("onTap") above: renaming this
    // method without updating that string breaks the button at runtime only.
    @ObjCAction
    fun onTap() = action()
}
