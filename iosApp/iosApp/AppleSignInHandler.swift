import AuthenticationServices
import CryptoKit
import FirebaseAuth
import Foundation
import UIKit
import shared

/// Swift side of AppleSignInBridge (Kotlin). Runs Sign in with Apple through
/// AuthenticationServices and reports back to the bridge.
///
/// - Sign-in: a random nonce is generated per request; Apple receives its SHA-256
///   and Firebase the raw value, so a stolen identity token cannot be replayed.
/// - Revocation (account deletion, App Store guideline 5.1.1(v)): Apple is asked
///   for a fresh authorization code, which Firebase exchanges and revokes. It needs
///   the Sign in with Apple key configured on the Firebase Apple provider; without
///   it Firebase fails and the deletion goes on (the Kotlin side logs the error).
class AppleSignInCallbackImpl: NSObject, AppleSignInCallback {

    private enum Purpose {
        case signIn(rawNonce: String)
        case revoke
    }

    private var purpose: Purpose?
    private var controller: ASAuthorizationController?

    func onSignInRequested() {
        guard let rawNonce = Self.randomNonce() else {
            AppleSignInBridge.shared.onSignInError(message: "Could not generate a secure nonce")
            return
        }
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = Self.sha256(rawNonce)
        perform(request, purpose: .signIn(rawNonce: rawNonce))
    }

    func onRevokeRequested() {
        // No scopes: only a fresh authorization code is needed.
        let request = ASAuthorizationAppleIDProvider().createRequest()
        perform(request, purpose: .revoke)
    }

    private func perform(_ request: ASAuthorizationAppleIDRequest, purpose: Purpose) {
        // A request still in flight is resolved as cancelled before starting another one.
        if let pending = self.purpose {
            finishCancelled(pending)
        }
        self.purpose = purpose
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        self.controller = controller
        controller.performRequests()
    }

    // MARK: - Results

    private func finishCancelled(_ purpose: Purpose) {
        switch purpose {
        case .signIn:
            AppleSignInBridge.shared.onSignInCancelled()
        case .revoke:
            AppleSignInBridge.shared.onRevokeError(message: "Sign in with Apple re-authorization cancelled")
        }
    }

    private func finishError(_ purpose: Purpose, message: String) {
        switch purpose {
        case .signIn:
            AppleSignInBridge.shared.onSignInError(message: message)
        case .revoke:
            AppleSignInBridge.shared.onRevokeError(message: message)
        }
    }

    private func handleSignIn(_ credential: ASAuthorizationAppleIDCredential, rawNonce: String) {
        guard let tokenData = credential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8) else {
            AppleSignInBridge.shared.onSignInError(message: "No identity token received from Apple")
            return
        }
        // Apple sends the name only the first time the user authorises the app.
        var fullName: String?
        if let components = credential.fullName {
            let formatted = PersonNameComponentsFormatter().string(from: components)
                .trimmingCharacters(in: .whitespacesAndNewlines)
            fullName = formatted.isEmpty ? nil : formatted
        }
        AppleSignInBridge.shared.onSignInSuccess(idToken: idToken, rawNonce: rawNonce, fullName: fullName)
    }

    private func handleRevoke(_ credential: ASAuthorizationAppleIDCredential) {
        guard let codeData = credential.authorizationCode,
              let authorizationCode = String(data: codeData, encoding: .utf8) else {
            AppleSignInBridge.shared.onRevokeError(message: "No authorization code received from Apple")
            return
        }
        Auth.auth().revokeToken(withAuthorizationCode: authorizationCode) { error in
            if let error = error {
                AppleSignInBridge.shared.onRevokeError(message: error.localizedDescription)
            } else {
                AppleSignInBridge.shared.onRevokeSuccess()
            }
        }
    }

    // MARK: - Nonce

    /// 32 random characters from a URL-safe charset, from the system CSPRNG.
    private static func randomNonce(length: Int = 32) -> String? {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var bytes = [UInt8](repeating: 0, count: length)
        guard SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes) == errSecSuccess else {
            return nil
        }
        return String(bytes.map { charset[Int($0) % charset.count] })
    }

    private static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
    }
}

// MARK: - ASAuthorizationControllerDelegate

extension AppleSignInCallbackImpl: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController,
                                 didCompleteWithAuthorization authorization: ASAuthorization) {
        guard controller === self.controller, let purpose = self.purpose else { return }
        self.purpose = nil
        self.controller = nil
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
            finishError(purpose, message: "Unexpected credential type from Apple")
            return
        }
        switch purpose {
        case .signIn(let rawNonce):
            handleSignIn(credential, rawNonce: rawNonce)
        case .revoke:
            handleRevoke(credential)
        }
    }

    func authorizationController(controller: ASAuthorizationController,
                                 didCompleteWithError error: Error) {
        guard controller === self.controller, let purpose = self.purpose else { return }
        self.purpose = nil
        self.controller = nil
        if let authError = error as? ASAuthorizationError, authError.code == .canceled {
            finishCancelled(purpose)
        } else {
            finishError(purpose, message: error.localizedDescription)
        }
    }
}

// MARK: - ASAuthorizationControllerPresentationContextProviding

extension AppleSignInCallbackImpl: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        if let window = scenes.flatMap({ $0.windows }).first(where: { $0.isKeyWindow }) {
            return window
        }
        return ASPresentationAnchor()
    }
}
