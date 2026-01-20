package com.motoparking.app.util

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable to request location permission.
 * On Android: Uses ActivityResultContracts
 * On iOS: Uses CLLocationManager.requestWhenInUseAuthorization()
 */
@Composable
expect fun RequestLocationPermission(
    onPermissionResult: (LocationPermissionStatus) -> Unit
)
