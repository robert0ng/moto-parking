package com.motoparking.app.ui.screens

import androidx.compose.runtime.Composable
import com.motoparking.shared.domain.model.ParkingSpot

/**
 * Platform-specific map screen implementation.
 * - Android: Google Maps with maps-compose
 * - iOS: Apple MapKit with UIKitView
 */
@Composable
expect fun MapScreen(
    parkingSpots: List<ParkingSpot>,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedRadius: Int,
    onSpotClick: (ParkingSpot) -> Unit
)

/**
 * Mini map for displaying a single parking spot location.
 * Used in DetailScreen.
 */
@Composable
expect fun MiniMap(
    spot: ParkingSpot,
    modifier: androidx.compose.ui.Modifier
)
