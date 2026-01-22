package com.motoparking.app

import androidx.compose.ui.window.ComposeUIViewController
import com.motoparking.app.util.IOSGoogleSignIn

fun MainViewController() = ComposeUIViewController { App() }

// Expose function for Swift to call when Google Sign-In completes
fun onGoogleSignInComplete(idToken: String?, accessToken: String?, error: String?) {
    IOSGoogleSignIn.onSignInResult(idToken, accessToken, error)
}

// Expose function for Swift to trigger sign-in request
private var signInRequestCallback: (() -> Unit)? = null

fun setSignInRequestCallback(callback: () -> Unit) {
    signInRequestCallback = callback
}

fun requestGoogleSignIn() {
    signInRequestCallback?.invoke()
}
