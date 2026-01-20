package com.motoparking.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motoparking.shared.data.repository.AuthRepository
import com.motoparking.shared.data.repository.ParkingRepository
import com.motoparking.shared.domain.model.ParkingSpot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val spot: ParkingSpot? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val isFavoriteLoading: Boolean = false,
    val isReportLoading: Boolean = false,
    val reportSuccess: Boolean = false,
    val reportError: String? = null,
    val checkInCount: Int = 0,
    val isCheckInLoading: Boolean = false,
    val checkInSuccess: Boolean = false,
    val checkInError: String? = null,
    val requiresAuth: Boolean = false,
    val error: String? = null
)

class DetailViewModel(
    private val repository: ParkingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var currentSpotId: String? = null

    fun loadSpot(spotId: String) {
        currentSpotId = spotId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val spot = repository.getParkingSpotById(spotId)
                _uiState.value = _uiState.value.copy(
                    spot = spot,
                    isLoading = false,
                    error = if (spot == null) "找不到停車位資料" else null
                )

                // Check favorite status if authenticated
                checkFavoriteStatus(spotId)

                // Load check-in status
                loadCheckInStatus(spotId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "載入失敗"
                )
            }
        }
    }

    private suspend fun checkFavoriteStatus(spotId: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val isFav = repository.isFavorite(userId, spotId)
            _uiState.value = _uiState.value.copy(isFavorite = isFav)
        }
    }

    fun toggleFavorite() {
        val spotId = currentSpotId ?: return
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            // User not authenticated - signal to show login prompt
            _uiState.value = _uiState.value.copy(requiresAuth = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFavoriteLoading = true)
            try {
                if (_uiState.value.isFavorite) {
                    repository.removeFromFavorites(userId, spotId)
                    _uiState.value = _uiState.value.copy(
                        isFavorite = false,
                        isFavoriteLoading = false
                    )
                } else {
                    repository.addToFavorites(userId, spotId)
                    _uiState.value = _uiState.value.copy(
                        isFavorite = true,
                        isFavoriteLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isFavoriteLoading = false,
                    error = e.message ?: "操作失敗"
                )
            }
        }
    }

    fun clearAuthRequired() {
        _uiState.value = _uiState.value.copy(requiresAuth = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Refresh favorite status after auth state changes
     */
    fun refreshFavoriteStatus() {
        val spotId = currentSpotId ?: return
        viewModelScope.launch {
            checkFavoriteStatus(spotId)
        }
    }

    /**
     * Check if user is authenticated (for report feature)
     */
    fun isAuthenticated(): Boolean {
        return authRepository.isAuthenticated()
    }

    /**
     * Submit a report for the current spot
     */
    fun submitReport(category: String, comment: String?) {
        val spotId = currentSpotId ?: return
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = _uiState.value.copy(requiresAuth = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReportLoading = true, reportError = null)
            try {
                repository.submitReport(userId, spotId, category, comment)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isReportLoading = false,
                            reportSuccess = true
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isReportLoading = false,
                            reportError = e.message ?: "回報失敗"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isReportLoading = false,
                    reportError = e.message ?: "回報失敗"
                )
            }
        }
    }

    fun clearReportState() {
        _uiState.value = _uiState.value.copy(
            reportSuccess = false,
            reportError = null
        )
    }

    /**
     * Check in to the current spot
     */
    fun checkIn() {
        val spotId = currentSpotId ?: return
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = _uiState.value.copy(requiresAuth = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckInLoading = true, checkInError = null)
            try {
                repository.checkIn(userId, spotId)
                    .onSuccess {
                        // Refresh check-in count
                        val newCount = repository.getCheckInCount(spotId)
                        _uiState.value = _uiState.value.copy(
                            isCheckInLoading = false,
                            checkInCount = newCount,
                            checkInSuccess = true
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isCheckInLoading = false,
                            checkInError = e.message ?: "打卡失敗"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckInLoading = false,
                    checkInError = e.message ?: "打卡失敗"
                )
            }
        }
    }

    fun clearCheckInState() {
        _uiState.value = _uiState.value.copy(
            checkInSuccess = false,
            checkInError = null
        )
    }

    /**
     * Load check-in count for the current spot
     */
    private suspend fun loadCheckInStatus(spotId: String) {
        val count = repository.getCheckInCount(spotId)
        _uiState.value = _uiState.value.copy(checkInCount = count)
    }
}
