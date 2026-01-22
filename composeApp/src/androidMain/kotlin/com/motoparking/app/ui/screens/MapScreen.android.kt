package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
    onSpotClick: (ParkingSpot) -> Unit
) {
    val userLocation = if (userLatitude != null && userLongitude != null) {
        LatLng(userLatitude, userLongitude)
    } else {
        DEFAULT_LOCATION
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, DEFAULT_ZOOM)
    }

    // Update camera when user location changes
    LaunchedEffect(userLatitude, userLongitude) {
        if (userLatitude != null && userLongitude != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLatitude, userLongitude),
                    DEFAULT_ZOOM
                )
            )
        }
    }

    // Fit camera to show all spots if they exist
    LaunchedEffect(parkingSpots) {
        if (parkingSpots.isNotEmpty() && parkingSpots.size <= 50) {
            val boundsBuilder = LatLngBounds.Builder()

            // Include user location
            if (userLatitude != null && userLongitude != null) {
                boundsBuilder.include(LatLng(userLatitude, userLongitude))
            }

            // Include all parking spots
            parkingSpots.forEach { spot ->
                boundsBuilder.include(LatLng(spot.latitude, spot.longitude))
            }

            try {
                val bounds = boundsBuilder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                )
            } catch (e: Exception) {
                // Fallback to user location if bounds calculation fails
            }
        }
    }

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
            // Add markers for each parking spot
            parkingSpots.forEach { spot ->
                Marker(
                    state = MarkerState(
                        position = LatLng(spot.latitude, spot.longitude)
                    ),
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
