package com.motoparking.app.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

/**
 * iOS implementation of Google Sign-In.
 * Uses native GoogleSignIn SDK via Swift helper.
 */
@Composable
actual fun GoogleSignInButton(
    onSignInResult: (GoogleSignInResult) -> Unit,
    onSignInStarted: () -> Unit,
    enabled: Boolean
) {
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isLoading) {
                isLoading = true
                onSignInStarted()
                IOSGoogleSignIn.signIn { result ->
                    isLoading = false
                    onSignInResult(result)
                }
            }
        },
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text("G")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isLoading) "登入中..." else "使用 Google 登入")
    }
}

/**
 * iOS Google Sign-In handler.
 * The actual sign-in is performed by the Swift GoogleSignInHelper.
 * Bridge is set up in MainViewController.kt and ContentView.swift
 */
object IOSGoogleSignIn {
    private var callback: ((GoogleSignInResult) -> Unit)? = null

    fun signIn(onResult: (GoogleSignInResult) -> Unit) {
        callback = onResult
        // Trigger sign-in request via the bridge (calls Swift)
        com.motoparking.app.requestGoogleSignIn()
    }

    // Called from Swift via MainViewControllerKt.onGoogleSignInComplete
    fun onSignInResult(idToken: String?, accessToken: String?, error: String?) {
        val result = when {
            idToken != null -> GoogleSignInResult.Success(idToken, accessToken)
            error != null -> GoogleSignInResult.Error(error)
            else -> GoogleSignInResult.Cancelled
        }
        callback?.invoke(result)
        callback = null
    }
}
