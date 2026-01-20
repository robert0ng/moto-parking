package com.motoparking.shared.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Simple user data class exposed to the app layer.
 */
data class AppUser(
    val id: String,
    val email: String?,
    val name: String?,
    val avatarUrl: String?
)

/**
 * Authentication state
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(val user: AppUser) : AuthState()
}

/**
 * Repository for handling authentication operations.
 */
class AuthRepository(
    private val supabaseClient: SupabaseClient
) {
    /**
     * Observe the current authentication state.
     */
    val authState: Flow<AuthState> = supabaseClient.auth.sessionStatus.map { status ->
        when {
            status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                val user = supabaseClient.auth.currentUserOrNull()
                if (user != null) {
                    AuthState.Authenticated(user.toAppUser())
                } else {
                    AuthState.NotAuthenticated
                }
            }
            status is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated -> {
                AuthState.NotAuthenticated
            }
            else -> AuthState.Loading
        }
    }

    /**
     * Get the current user or null if not authenticated.
     */
    fun getCurrentUser(): AppUser? {
        return supabaseClient.auth.currentUserOrNull()?.toAppUser()
    }

    /**
     * Get the current user ID or null if not authenticated.
     */
    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    /**
     * Check if user is currently authenticated.
     */
    fun isAuthenticated(): Boolean {
        return supabaseClient.auth.currentUserOrNull() != null
    }

    /**
     * Sign in with Google ID token (obtained from platform-specific Google Sign-In).
     * @param idToken The ID token from Google Sign-In
     * @param accessToken Optional access token from Google Sign-In
     */
    suspend fun signInWithGoogle(idToken: String, accessToken: String? = null): Result<AppUser> {
        return try {
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
                accessToken?.let { this.accessToken = it }
            }
            val user = supabaseClient.auth.currentUserOrNull()
            if (user != null) {
                Result.success(user.toAppUser())
            } else {
                Result.failure(Exception("Sign in succeeded but user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert Supabase UserInfo to AppUser.
     */
    private fun io.github.jan.supabase.auth.user.UserInfo.toAppUser(): AppUser {
        return AppUser(
            id = this.id,
            email = this.email,
            name = this.userMetadata?.get("full_name")?.toString()?.trim('"'),
            avatarUrl = this.userMetadata?.get("avatar_url")?.toString()?.trim('"')
        )
    }
}
