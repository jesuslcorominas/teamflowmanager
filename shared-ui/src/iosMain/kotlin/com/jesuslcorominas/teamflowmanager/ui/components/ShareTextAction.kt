package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberShareTextAction(): (String) -> Unit =
    remember {
        { text ->
            val presenter = topViewController()
            if (presenter != null) {
                val activityVC =
                    UIActivityViewController(
                        activityItems = listOf(text),
                        applicationActivities = null,
                    )
                // iPad requires an anchor for the popover; without it UIKit throws.
                activityVC.popoverPresentationController?.sourceView = presenter.view
                presenter.presentViewController(activityVC, animated = true, completion = null)
            }
        }
    }

private fun topViewController(): UIViewController? {
    val window =
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { it.isKeyWindow() }
    var top = window?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
