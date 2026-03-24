import SwiftUI
import shared
import FirebaseCore
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        // KMP-17: configure GIDSignIn with the CLIENT_ID from GoogleService-Info.plist
        // (loaded by FirebaseApp.configure() — no need to read the plist again)
        if let clientID = FirebaseApp.app()?.options.clientID {
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        }
        MainViewControllerKt.doInitKoinIos()
        // KMP-17: register the native Google Sign-In handler
        GoogleSignInBridge.shared.callback = GoogleSignInCallbackImpl()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
