package com.motoparking.app.util

import androidx.compose.runtime.*

/**
 * iOS implementation of location permission request.
 * Uses CLLocationManager from LocationService.
 */
@Composable
actual fun RequestLocationPermission(
    onPermissionResult: (LocationPermissionStatus) -> Unit
) {
    val locationService = remember { LocationService() }

    LaunchedEffect(Unit) {
        val currentStatus = locationService.checkPermissionStatus()
        if (currentStatus == LocationPermissionStatus.NOT_DETERMINED) {
            locationService.requestPermission { status ->
                onPermissionResult(status)
            }
        } else {
            onPermissionResult(currentStatus)
        }
    }
}
