import UIKit
import SwiftUI
import ComposeApp
import GoogleSignIn

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewControllerKt.MainViewController()

        // Set up Google Sign-In bridge - connect Kotlin requests to Swift handler
        MainViewControllerKt.setSignInRequestCallback {
            GoogleSignInHelper.shared.signIn { idToken, accessToken, error in
                // Call back to Kotlin with the result
                MainViewControllerKt.onGoogleSignInComplete(idToken: idToken, accessToken: accessToken, error: error)
            }
        }

        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handling
    }
}
