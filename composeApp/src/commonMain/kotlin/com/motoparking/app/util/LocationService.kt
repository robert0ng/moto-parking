package com.motoparking.app.util

/**
 * Location data class to hold coordinates.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * Default location: Taipei Main Station
 */
val DEFAULT_LOCATION = Location(
    latitude = 25.0478,
    longitude = 121.5170
)

/**
 * Location permission status.
 */
enum class LocationPermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED
}

/**
 * Platform-specific location service interface.
 */
expect class LocationService() {
    /**
     * Check if location permission is granted.
     */
    fun checkPermissionStatus(): LocationPermissionStatus

    /**
     * Request location permission.
     * @param onResult Callback with permission result
     */
    fun requestPermission(onResult: (LocationPermissionStatus) -> Unit)

    /**
     * Get current location.
     * @param onSuccess Callback with location on success
     * @param onError Callback on error (will use default location)
     */
    fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    )
}
