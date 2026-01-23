package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
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
                    val markerState = rememberMarkerState(
                        key = spot.id,
                        position = LatLng(spot.latitude, spot.longitude)
                    )
                    Marker(
                        state = markerState,
                        title = spot.name,
                        snippet = spot.address,
                        onClick = {
                            onSpotClick(spot)
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
