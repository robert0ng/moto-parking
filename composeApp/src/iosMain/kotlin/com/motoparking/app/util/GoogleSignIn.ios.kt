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

/**
 * iOS implementation of Google Sign-In.
 * TODO: Integrate Google Sign-In SDK via CocoaPods for production.
 * For now, this shows a placeholder button.
 */
@Composable
actual fun GoogleSignInButton(
    onSignInResult: (GoogleSignInResult) -> Unit,
    enabled: Boolean
) {
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            // TODO: Implement iOS Google Sign-In with Google Sign-In SDK
            // For now, show an error message
            onSignInResult(GoogleSignInResult.Error("iOS Google 登入尚未實作，敬請期待"))
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
