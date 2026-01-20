package com.motoparking.app.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class LocationService {
    private val locationManager = CLLocationManager()
    private var permissionCallback: ((LocationPermissionStatus) -> Unit)? = null
    private var locationCallback: ((Location) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(
            manager: CLLocationManager,
            didChangeAuthorizationStatus: CLAuthorizationStatus
        ) {
            val status = mapAuthorizationStatus(didChangeAuthorizationStatus)
            permissionCallback?.invoke(status)
            permissionCallback = null
        }

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>
        ) {
            val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
            if (clLocation != null) {
                val coordinate = clLocation.coordinate.useContents {
                    Location(latitude = this.latitude, longitude = this.longitude)
                }
                locationCallback?.invoke(coordinate)
            } else {
                errorCallback?.invoke("Unable to get location")
            }
            locationCallback = null
            errorCallback = null
            locationManager.stopUpdatingLocation()
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: NSError
        ) {
            errorCallback?.invoke(didFailWithError.localizedDescription)
            errorCallback = null
            locationCallback = null
            locationManager.stopUpdatingLocation()
        }
    }

    init {
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }

    actual fun checkPermissionStatus(): LocationPermissionStatus {
        return mapAuthorizationStatus(locationManager.authorizationStatus)
    }

    actual fun requestPermission(onResult: (LocationPermissionStatus) -> Unit) {
        val currentStatus = checkPermissionStatus()
        if (currentStatus != LocationPermissionStatus.NOT_DETERMINED) {
            onResult(currentStatus)
            return
        }

        permissionCallback = onResult
        locationManager.requestWhenInUseAuthorization()
    }

    actual fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        if (checkPermissionStatus() != LocationPermissionStatus.GRANTED) {
            onError("Location permission not granted")
            return
        }

        locationCallback = onSuccess
        errorCallback = onError
        locationManager.startUpdatingLocation()
    }

    private fun mapAuthorizationStatus(status: CLAuthorizationStatus): LocationPermissionStatus {
        return when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionStatus.GRANTED
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> LocationPermissionStatus.DENIED
            kCLAuthorizationStatusNotDetermined -> LocationPermissionStatus.NOT_DETERMINED
            else -> LocationPermissionStatus.DENIED
        }
    }
}
