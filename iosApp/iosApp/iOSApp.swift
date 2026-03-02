import SwiftUI
import shared
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        MainViewControllerKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
