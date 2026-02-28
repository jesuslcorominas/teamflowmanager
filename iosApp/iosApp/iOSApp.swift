import SwiftUI
import shared
import FirebaseCore
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        MainViewControllerKt.doInitKoinIos()
        // KMP-17: register the native Google Sign-In handler
        GoogleSignInBridge.shared.callback = GoogleSignInCallbackImpl()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
