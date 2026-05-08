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

    /**
     * Get the administrative area (city + district) from coordinates,
     * regardless of whether the point is on a named street. Used for
     * matching against policy zones — returns a string like "新北市 板橋區".
     */
    fun getAdministrativeArea(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    )
}
