package com.motoparking.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motoparking.shared.data.repository.AuthRepository
import com.motoparking.shared.data.repository.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null,
    val userAvatarUrl: String? = null,
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.value = when (authState) {
                    is AuthState.Loading -> _uiState.value.copy(isLoading = true)
                    is AuthState.NotAuthenticated -> AuthUiState(
                        isLoading = false,
                        isAuthenticated = false
                    )
                    is AuthState.Authenticated -> AuthUiState(
                        isLoading = false,
                        isAuthenticated = true,
                        userName = authState.user.name,
                        userEmail = authState.user.email,
                        userAvatarUrl = authState.user.avatarUrl
                    )
                }
            }
        }
    }

    /**
     * Sign in with Google ID token (called from platform-specific Google Sign-In).
     */
    fun signInWithGoogle(idToken: String, accessToken: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.signInWithGoogle(idToken, accessToken)
                .onSuccess {
                    // State will be updated by observeAuthState
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "登入失敗"
                    )
                }
        }
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.signOut()
                .onSuccess {
                    // State will be updated by observeAuthState
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "登出失敗"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
