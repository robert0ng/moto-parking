package com.motoparking.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motoparking.shared.data.repository.ParkingRepository
import com.motoparking.shared.domain.model.PolicySegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PolicySegmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** All loaded segments (including geometry-less ones for list views). */
    val segments: List<PolicySegment> = emptyList(),
    /** Subset of [segments] with non-null geometry — what the map overlay should draw. */
    val drawable: List<PolicySegment> = emptyList()
)

/**
 * Loads the plate-policy road segments (Layer B) once per session and exposes
 * them for map overlay rendering. Mirrors the [PolicyViewModel] / [ParkingListViewModel]
 * pattern used elsewhere in the app.
 */
class PolicySegmentViewModel(
    private val repository: ParkingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PolicySegmentUiState())
    val uiState: StateFlow<PolicySegmentUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val segments = repository.getAllPolicySegments()
                _uiState.value = PolicySegmentUiState(
                    isLoading = false,
                    segments = segments,
                    drawable = segments.filter { it.geometry != null }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "載入路段資料失敗"
                )
            }
        }
    }

    fun retry() = load()
}
