package com.motoparking.app.util

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

/**
 * Composable that handles Android location permission request.
 * @param onPermissionResult Callback with the permission result
 */
@Composable
actual fun RequestLocationPermission(
    onPermissionResult: (LocationPermissionStatus) -> Unit
) {
    val locationService = remember { LocationService() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        val status = if (fineLocationGranted || coarseLocationGranted) {
            LocationPermissionStatus.GRANTED
        } else {
            LocationPermissionStatus.DENIED
        }
        onPermissionResult(status)
    }

    LaunchedEffect(Unit) {
        val currentStatus = locationService.checkPermissionStatus()
        if (currentStatus == LocationPermissionStatus.NOT_DETERMINED ||
            currentStatus == LocationPermissionStatus.DENIED) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            onPermissionResult(currentStatus)
        }
    }
}
