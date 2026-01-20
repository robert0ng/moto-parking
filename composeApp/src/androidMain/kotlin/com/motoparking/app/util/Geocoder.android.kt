package com.motoparking.app.util

import android.location.Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

actual class Geocoder {

    actual fun getLocationName(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        val context = AndroidContextProvider.getContext()
        if (context == null) {
            onResult(null)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = android.location.Geocoder(context, Locale.TAIWAN)

                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                val locationName = addresses?.firstOrNull()?.let { address ->
                    // Try to get the most specific location name
                    // Priority: thoroughfare (street) > subLocality > locality
                    address.thoroughfare  // Street name like "敦化南路二段"
                        ?: address.subLocality  // District/area
                        ?: address.locality  // City
                        ?: address.subAdminArea
                }

                withContext(Dispatchers.Main) {
                    onResult(locationName)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }
}
