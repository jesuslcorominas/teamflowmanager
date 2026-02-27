import SwiftUI
import iosApp

@main
struct iOSApp: App {
    init() {
        IosModuleKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
