import SwiftUI
import shared
import FirebaseCore
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        #if DEBUG
        FirebaseApp.configure()
        #else
        if let path = Bundle.main.path(forResource: "GoogleService-Info-Prod", ofType: "plist"),
           let options = FirebaseOptions(contentsOfFile: path) {
            FirebaseApp.configure(options: options)
        } else {
            FirebaseApp.configure()
        }
        #endif
        // KMP-17: configure GIDSignIn with the CLIENT_ID from the loaded GoogleService-Info plist
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
