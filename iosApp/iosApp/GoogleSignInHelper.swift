import Foundation
import GoogleSignIn
import UIKit

@objc public class GoogleSignInHelper: NSObject {
    @objc public static let shared = GoogleSignInHelper()

    private override init() {
        super.init()
    }

    @objc public func signIn(
        completion: @escaping (_ idToken: String?, _ accessToken: String?, _ error: String?) -> Void
    ) {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else {
            completion(nil, nil, "無法取得視窗")
            return
        }

        // Find the topmost presented view controller to avoid overlap with dialogs
        let presentingViewController = Self.topMostViewController(from: rootViewController)

        // Get the client ID from Info.plist
        guard let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String else {
            completion(nil, nil, "找不到 Google Client ID")
            return
        }

        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config

        GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) { result, error in
            if let error = error {
                completion(nil, nil, error.localizedDescription)
                return
            }

            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString else {
                completion(nil, nil, "無法取得 ID Token")
                return
            }

            let accessToken = user.accessToken.tokenString
            completion(idToken, accessToken, nil)
        }
    }

    @objc public func handleURL(_ url: URL) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }

    @objc public func signOut() {
        GIDSignIn.sharedInstance.signOut()
    }

    /// Find the topmost presented view controller from a given root
    private static func topMostViewController(from viewController: UIViewController) -> UIViewController {
        if let presented = viewController.presentedViewController {
            return topMostViewController(from: presented)
        }
        if let nav = viewController as? UINavigationController,
           let visible = nav.visibleViewController {
            return topMostViewController(from: visible)
        }
        if let tab = viewController as? UITabBarController,
           let selected = tab.selectedViewController {
            return topMostViewController(from: selected)
        }
        return viewController
    }
}
