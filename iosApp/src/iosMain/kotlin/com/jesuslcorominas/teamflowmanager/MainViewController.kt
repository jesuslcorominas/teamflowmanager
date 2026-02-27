package com.jesuslcorominas.teamflowmanager

import androidx.compose.ui.window.ComposeUIViewController
import com.jesuslcorominas.teamflowmanager.di.initKoinIos
import com.jesuslcorominas.teamflowmanager.ui.App
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
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
