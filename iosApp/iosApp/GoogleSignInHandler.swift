import Foundation
import GoogleSignIn
import UIKit
import shared

/// KMP-17: Swift implementation of GoogleSignInCallback (Kotlin interface).
/// Called from Kotlin's GoogleSignInBridge.signIn() coroutine when the user
/// taps the "Sign in with Google" button in the Compose LoginScreen.
class GoogleSignInCallbackImpl: NSObject, GoogleSignInCallback {

    func onSignInRequested() {
        guard let rootVC = topViewController() else {
            GoogleSignInBridge.shared.onError(message: "No root view controller available")
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            if let error = error {
                GoogleSignInBridge.shared.onError(message: error.localizedDescription)
                return
            }
            guard let idToken = result?.user.idToken?.tokenString else {
                GoogleSignInBridge.shared.onError(message: "No ID token received from Google Sign-In")
                return
            }
            GoogleSignInBridge.shared.onSuccess(idToken: idToken)
        }
    }

    // MARK: - Helpers

    private func topViewController() -> UIViewController? {
        let scenes = UIApplication.shared.connectedScenes
        let windowScene = scenes.first(where: { $0 is UIWindowScene }) as? UIWindowScene
        let window = windowScene?.windows.first(where: { $0.isKeyWindow })
        return topViewController(from: window?.rootViewController)
    }

    private func topViewController(from root: UIViewController?) -> UIViewController? {
        if let nav = root as? UINavigationController {
            return topViewController(from: nav.visibleViewController)
        }
        if let tab = root as? UITabBarController {
            return topViewController(from: tab.selectedViewController)
        }
        if let presented = root?.presentedViewController {
            return topViewController(from: presented)
        }
        return root
    }
}
