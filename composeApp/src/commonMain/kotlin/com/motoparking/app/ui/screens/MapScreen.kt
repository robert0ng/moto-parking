package com.motoparking.app.ui.screens

import androidx.compose.runtime.Composable
import com.motoparking.shared.domain.model.ParkingSpot
import com.motoparking.shared.domain.model.PolicySegment

/**
 * Platform-specific map screen implementation.
 * - Android: Google Maps with maps-compose
 * - iOS: Apple MapKit with UIKitView
 *
 * `policySegments` are the plate-policy road segments (Layer B). Only segments
 * with non-null geometry are drawn; pass `drawable` from PolicySegmentViewModel.
 */
@Composable
expect fun MapScreen(
    parkingSpots: List<ParkingSpot>,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedRadius: Int,
    onSpotClick: (ParkingSpot) -> Unit,
    onMapCenterChanged: ((latitude: Double, longitude: Double, viewportRadiusMeters: Int) -> Unit)? = null,
    policySegments: List<PolicySegment> = emptyList()
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
