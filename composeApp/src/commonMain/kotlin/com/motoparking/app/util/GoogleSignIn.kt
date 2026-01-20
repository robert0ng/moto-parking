package com.motoparking.app.util

import androidx.compose.runtime.Composable

/**
 * Result from Google Sign-In attempt.
 */
sealed class GoogleSignInResult {
    data class Success(val idToken: String, val accessToken: String? = null) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
}

/**
 * Platform-specific Google Sign-In button composable.
 * Handles the entire sign-in flow and returns the ID token.
 */
@Composable
expect fun GoogleSignInButton(
    onSignInResult: (GoogleSignInResult) -> Unit,
    enabled: Boolean = true
)
