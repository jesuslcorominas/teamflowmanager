package com.jesuslcorominas.teamflowmanager

import androidx.compose.ui.window.ComposeUIViewController
import com.jesuslcorominas.teamflowmanager.di.initKoinIos as diInitKoinIos
import com.jesuslcorominas.teamflowmanager.ui.App
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point for the Compose Multiplatform UI.
 *
 * Call from Swift AppDelegate / @main struct:
 *
 * ```swift
 * import iosApp
 *
 * @main
 * struct iOSApp: App {
 *     init() { MainViewControllerKt.doInitKoinIos() }  // via KMP-13
 *     var body: some Scene {
 *         WindowGroup {
 *             ContentView()
 *         }
 *     }
 * }
 *
 * struct ContentView: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainViewControllerKt.MainViewController()
 *     }
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 */
/**
 * Re-exports the Koin iOS initializer so Swift can call:
 *   MainViewControllerKt.doInitKoinIos()
 *
 * Swift sees `init` as a keyword so Kotlin/Native adds the `do` prefix automatically.
 */
fun initKoinIos() = diInitKoinIos()

fun MainViewController(): UIViewController {
    var vc: UIViewController? = null
    val result = ComposeUIViewController(configure = { enforceStrictPlistSanityCheck = false }) {
        App(
            onSignInWithGoogle = { GoogleSignInBridge.signIn() },
            onShareFile = { filePath ->
                val fileUrl = NSURL.fileURLWithPath(filePath)
                val activityVC = UIActivityViewController(
                    activityItems = listOf(fileUrl),
                    applicationActivities = null,
                )
                vc?.presentViewController(activityVC, animated = true, completion = null)
            },
        )
    }
    vc = result
    return result
}
