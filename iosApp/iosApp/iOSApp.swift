import SwiftUI
import shared
import FirebaseCore
import FirebaseMessaging
import GoogleSignIn
import UserNotifications

@main
struct iOSApp: App {
    // Push notifications (#362): APNs registration and delivery go through UIKit's app delegate.
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

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

/// Push notifications over APNs + Firebase Cloud Messaging (#362).
///
/// `iOSApp.init` configures Firebase before this delegate receives `didFinishLaunching`.
/// The permission is asked at launch, as `MainActivity` does on Android; the system only shows
/// the dialog while the choice is undecided. The FCM token is read and stored in Firestore by the
/// shared `SyncFcmTokenUseCase` (splash, login, club changes), not here.
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        PushPermissionKt.requestPushPermission { granted in
            NSLog("TFMPush notification permission granted: %@", granted.boolValue ? "true" : "false")
        }
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // Set explicitly so it does not depend on Firebase's app delegate swizzling.
        Messaging.messaging().apnsToken = deviceToken
        NSLog("TFMPush APNs token received (%d bytes)", deviceToken.count)
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        NSLog("TFMPush APNs registration failed: %@", error.localizedDescription)
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        // Only the length and the tail are logged: the full token addresses this device.
        let tail = fcmToken.map { String($0.suffix(8)) } ?? "nil"
        NSLog("TFMPush FCM token received (length %d, ...%@)", fcmToken?.count ?? 0, tail)
    }

    // Foreground: show the banner like the system does in the background. Android's
    // `TeamFlowFirebaseMessagingService` likewise posts a notification while the app is open.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .sound])
    }

    // Tap on a notification: opening the app is all Android does too.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }
}
