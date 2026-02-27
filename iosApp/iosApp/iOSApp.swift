import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
