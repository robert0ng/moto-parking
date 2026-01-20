package com.motoparking.app.util

/**
 * Platform-specific reverse geocoding to get location name from coordinates.
 */
expect class Geocoder() {
    /**
     * Get a short location name (street/area) from coordinates.
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param onResult Callback with the location name (or null if failed)
     */
    fun getLocationName(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    )
}
