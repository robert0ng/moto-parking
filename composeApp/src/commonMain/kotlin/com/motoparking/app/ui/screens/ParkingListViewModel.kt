package com.motoparking.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motoparking.shared.data.repository.ParkingRepository
import com.motoparking.shared.domain.model.ParkingSpot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParkingListUiState(
    val spots: List<ParkingSpot> = emptyList(),
    val filteredSpots: List<ParkingSpot> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
)

class ParkingListViewModel(
    private val repository: ParkingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParkingListUiState())
    val uiState: StateFlow<ParkingListUiState> = _uiState.asStateFlow()

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var currentRadius: Int = 1000
    private var currentOffset: Int = 0
    private val pageSize: Int = 20

    fun loadNearbySpots(latitude: Double, longitude: Double, radiusMeters: Int = 1000) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentRadius = radiusMeters
        currentOffset = 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, hasMore = true)
            try {
                val spots = repository.getNearbyParkingSpots(
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = radiusMeters,
                    offset = 0,
                    limit = pageSize
                )
                _uiState.value = _uiState.value.copy(
                    spots = spots,
                    filteredSpots = filterSpots(spots, _uiState.value.searchQuery),
                    isLoading = false,
                    hasMore = spots.size >= pageSize
                )
                currentOffset = spots.size
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "載入失敗"
                )
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            try {
                val moreSpots = repository.getNearbyParkingSpots(
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    radiusMeters = currentRadius,
                    offset = currentOffset,
                    limit = pageSize
                )
                // Deduplicate by ID to prevent LazyColumn key crashes
                val allSpots = (_uiState.value.spots + moreSpots).distinctBy { it.id }
                _uiState.value = _uiState.value.copy(
                    spots = allSpots,
                    filteredSpots = filterSpots(allSpots, _uiState.value.searchQuery),
                    isLoadingMore = false,
                    hasMore = moreSpots.size >= pageSize
                )
                currentOffset += moreSpots.size
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "載入更多失敗"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredSpots = filterSpots(_uiState.value.spots, query)
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun filterSpots(spots: List<ParkingSpot>, query: String): List<ParkingSpot> {
        if (query.isBlank()) return spots
        val lowerQuery = query.lowercase()
        return spots.filter { spot ->
            spot.name.lowercase().contains(lowerQuery) ||
            spot.address.lowercase().contains(lowerQuery)
        }
    }
}
