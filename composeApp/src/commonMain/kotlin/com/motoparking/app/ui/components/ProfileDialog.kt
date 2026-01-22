package com.motoparking.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.motoparking.app.ui.viewmodels.AuthUiState
import com.motoparking.app.util.GoogleSignInButton
import com.motoparking.app.util.GoogleSignInResult

@Composable
fun ProfileDialog(
    authState: AuthUiState,
    onGoogleSignInResult: (GoogleSignInResult) -> Unit,
    onGoogleSignInStarted: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (authState.isAuthenticated) "個人資料" else "登入",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "關閉")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (authState.isAuthenticated) {
                    // User is logged in - show profile info
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    authState.userName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    authState.userEmail?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        enabled = !authState.isLoading
                    ) {
                        Text("登出")
                    }
                } else {
                    // User is not logged in - show sign in options
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "登入以使用收藏、打卡、回報功能",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GoogleSignInButton(
                        onSignInResult = onGoogleSignInResult,
                        onSignInStarted = onGoogleSignInStarted,
                        enabled = !authState.isLoading
                    )
                }

                // Show error if any
                authState.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // Show loading indicator
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
