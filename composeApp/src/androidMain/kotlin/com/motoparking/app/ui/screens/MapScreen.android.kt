package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.motoparking.shared.domain.model.ParkingSpot
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

// Default location: Taipei Main Station
private val DEFAULT_LOCATION = LatLng(25.0478, 121.5170)
private const val DEFAULT_ZOOM = 15f

@Composable
actual fun MapScreen(
    parkingSpots: List<ParkingSpot>,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedRadius: Int,
    onSpotClick: (ParkingSpot) -> Unit,
    onMapCenterChanged: ((latitude: Double, longitude: Double) -> Unit)?
) {
    val userLocation = if (userLatitude != null && userLongitude != null) {
        LatLng(userLatitude, userLongitude)
    } else {
        DEFAULT_LOCATION
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, DEFAULT_ZOOM)
    }
    val coroutineScope = rememberCoroutineScope()

    // Track currently selected/enlarged spot
    var selectedSpotId by remember { mutableStateOf<String?>(null) }

    // Track camera position changes and report when camera stops moving
    LaunchedEffect(onMapCenterChanged) {
        if (onMapCenterChanged != null) {
            snapshotFlow { cameraPositionState.isMoving }
                .filter { !it } // Only emit when camera stops moving
                .collectLatest {
                    val center = cameraPositionState.position.target
                    onMapCenterChanged(center.latitude, center.longitude)
                }
        }
    }

    // Note: We intentionally do NOT auto-reposition the camera when spots load
    // or when user location changes. The user controls the map position by panning.
    // They can use "Search this area" to search where they've panned to,
    // or use the built-in "my location" button to recenter on themselves.

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // Add markers for each parking spot (use distinctBy to avoid duplicate keys)
            parkingSpots.distinctBy { it.id }.forEach { spot ->
                key(spot.id) {
                    val isSelected = selectedSpotId == spot.id
                    val markerState = rememberMarkerState(
                        key = spot.id,
                        position = LatLng(spot.latitude, spot.longitude)
                    )
                    Marker(
                        state = markerState,
                        title = spot.name,
                        snippet = spot.address,
                        // All markers use red color - selection is indicated by zIndex and info window
                        // Create icons lazily (not in remember block) to ensure Google Play Services is initialized
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        // Bring selected marker to front
                        zIndex = if (isSelected) 1f else 0f,
                        // Scale up alpha slightly for selected marker visibility
                        alpha = if (isSelected) 1f else 0.85f,
                        onClick = {
                            if (isSelected) {
                                // Second tap on selected marker - navigate to detail
                                onSpotClick(spot)
                            } else {
                                // First tap - select and center on this marker
                                selectedSpotId = spot.id
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(
                                            LatLng(spot.latitude, spot.longitude)
                                        ),
                                        durationMs = 300
                                    )
                                }
                                // Show info window to indicate selection
                                it.showInfoWindow()
                            }
                            true // Consume the click
                        }
                    )
                }
            }
        }
    }
}

@Composable
actual fun MiniMap(
    spot: ParkingSpot,
    modifier: Modifier
) {
    val spotLocation = LatLng(spot.latitude, spot.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(spotLocation, 16f)
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        Marker(
            state = MarkerState(position = spotLocation),
            title = spot.name
        )
    }
}
