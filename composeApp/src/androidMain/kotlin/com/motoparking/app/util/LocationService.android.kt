package com.motoparking.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

actual class LocationService {

    actual fun checkPermissionStatus(): LocationPermissionStatus {
        val context = AndroidContextProvider.getContext() ?: return LocationPermissionStatus.DENIED

        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return if (fineLocation == PackageManager.PERMISSION_GRANTED ||
            coarseLocation == PackageManager.PERMISSION_GRANTED) {
            LocationPermissionStatus.GRANTED
        } else {
            LocationPermissionStatus.DENIED
        }
    }

    actual fun requestPermission(onResult: (LocationPermissionStatus) -> Unit) {
        // Permission request needs to be handled at the Activity level
        // This will be called from a composable that handles the permission request
        // For now, just check current status
        onResult(checkPermissionStatus())
    }

    @SuppressLint("MissingPermission")
    actual fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        val context = AndroidContextProvider.getContext()
        if (context == null) {
            onError("Context not available")
            return
        }

        if (checkPermissionStatus() != LocationPermissionStatus.GRANTED) {
            onError("Location permission not granted")
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { androidLocation ->
                if (androidLocation != null) {
                    onSuccess(Location(androidLocation.latitude, androidLocation.longitude))
                } else {
                    // Try last known location as fallback
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            if (lastLocation != null) {
                                onSuccess(Location(lastLocation.latitude, lastLocation.longitude))
                            } else {
                                onError("Unable to get location")
                            }
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Unknown error")
                        }
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Unknown error")
            }
    }
}
