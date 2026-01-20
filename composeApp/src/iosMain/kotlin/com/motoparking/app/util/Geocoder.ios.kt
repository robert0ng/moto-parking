package com.motoparking.app.util

import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark

actual class Geocoder {
    private val geocoder = CLGeocoder()

    actual fun getLocationName(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        val location = CLLocation(latitude = latitude, longitude = longitude)

        geocoder.reverseGeocodeLocation(location) { placemarks, error ->
            if (error != null || placemarks == null) {
                onResult(null)
                return@reverseGeocodeLocation
            }

            val placemark = placemarks.firstOrNull() as? CLPlacemark
            val locationName = placemark?.let {
                // Try to get the most specific location name
                // Priority: thoroughfare (street) > subLocality > locality
                it.thoroughfare  // Street name
                    ?: it.subLocality  // District/area
                    ?: it.locality  // City
                    ?: it.subAdministrativeArea
            }

            onResult(locationName)
        }
    }
}
