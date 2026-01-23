package com.motoparking.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchLocationState(
    val latitude: Double?,
    val longitude: Double?,
    val name: String?,
    val isRestored: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val _searchLocation = MutableStateFlow(SearchLocationState(null, null, null))
    val searchLocation: StateFlow<SearchLocationState> = _searchLocation.asStateFlow()

    fun updateSearchLocation(latitude: Double, longitude: Double, name: String? = null) {
        _searchLocation.value = SearchLocationState(latitude, longitude, name, isRestored = false)
    }

    fun updateLocationName(name: String?) {
        val current = _searchLocation.value
        if (current.latitude != null && current.longitude != null) {
            _searchLocation.value = current.copy(name = name)
        }
    }
}
