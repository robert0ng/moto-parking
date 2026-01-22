import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GoogleSignInHelper.shared.handleURL(url)
                }
        }
    }
}
