package com.motoparking.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.motoparking.shared.data.remote.SupabaseConfig
import kotlinx.coroutines.launch

// Helper to find Activity from Context
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
actual fun GoogleSignInButton(
    onSignInResult: (GoogleSignInResult) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isLoading) {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val activity = context.findActivity()
                        if (activity == null) {
                            onSignInResult(GoogleSignInResult.Error("無法取得 Activity"))
                            isLoading = false
                            return@launch
                        }

                        val credentialManager = CredentialManager.create(context)

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(SupabaseConfig.GOOGLE_WEB_CLIENT_ID)
                            .setAutoSelectEnabled(false)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(
                            request = request,
                            context = activity
                        )

                        handleSignInResult(result, onSignInResult)
                    } catch (e: GetCredentialCancellationException) {
                        onSignInResult(GoogleSignInResult.Cancelled)
                    } catch (e: NoCredentialException) {
                        onSignInResult(GoogleSignInResult.Error("找不到 Google 帳號，請先在裝置上登入 Google"))
                    } catch (e: GetCredentialException) {
                        // Show detailed error for debugging
                        val errorType = e::class.simpleName
                        val errorMsg = e.message ?: "Unknown"
                        onSignInResult(GoogleSignInResult.Error("[$errorType] $errorMsg"))
                    } catch (e: Exception) {
                        val errorType = e::class.simpleName
                        val errorMsg = e.message ?: "Unknown"
                        onSignInResult(GoogleSignInResult.Error("[$errorType] $errorMsg"))
                    } finally {
                        isLoading = false
                    }
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

private fun handleSignInResult(
    result: GetCredentialResponse,
    onSignInResult: (GoogleSignInResult) -> Unit
) {
    val credential = result.credential

    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    onSignInResult(
                        GoogleSignInResult.Success(
                            idToken = googleIdTokenCredential.idToken
                        )
                    )
                } catch (e: GoogleIdTokenParsingException) {
                    onSignInResult(GoogleSignInResult.Error("Failed to parse Google ID token"))
                }
            } else {
                onSignInResult(GoogleSignInResult.Error("Unexpected credential type"))
            }
        }
        else -> {
            onSignInResult(GoogleSignInResult.Error("Unexpected credential type"))
        }
    }
}
