package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.motoparking.shared.domain.model.ParkingSpot
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIColor

// Default location: Taipei Main Station
private const val DEFAULT_LATITUDE = 25.0478
private const val DEFAULT_LONGITUDE = 121.5170
private const val DEFAULT_SPAN_METERS = 1000.0

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapScreen(
    parkingSpots: List<ParkingSpot>,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedRadius: Int,
    onSpotClick: (ParkingSpot) -> Unit
) {
    val centerLatitude = userLatitude ?: DEFAULT_LATITUDE
    val centerLongitude = userLongitude ?: DEFAULT_LONGITUDE
    val spanMeters = selectedRadius.toDouble().coerceAtLeast(DEFAULT_SPAN_METERS)

    // Create annotations from parking spots with spot data stored in tag
    val annotationsWithSpots = remember(parkingSpots) {
        parkingSpots.map { spot ->
            spot to MKPointAnnotation().apply {
                setCoordinate(CLLocationCoordinate2DMake(spot.latitude, spot.longitude))
                setTitle(spot.name)
                setSubtitle(spot.address)
            }
        }
    }

    // Store spot lookup for click handling
    val spotLookup = remember(annotationsWithSpots) {
        annotationsWithSpots.associate { (spot, annotation) -> annotation to spot }
    }

    UIKitView(
        factory = {
            MKMapView().apply {
                showsUserLocation = true

                // Set initial region
                val center = CLLocationCoordinate2DMake(centerLatitude, centerLongitude)
                val region = MKCoordinateRegionMakeWithDistance(center, spanMeters * 2, spanMeters * 2)
                setRegion(region, animated = false)
            }
        },
        update = { mapView ->
            // Update region when location changes
            val center = CLLocationCoordinate2DMake(centerLatitude, centerLongitude)
            val region = MKCoordinateRegionMakeWithDistance(center, spanMeters * 2, spanMeters * 2)
            mapView.setRegion(region, animated = true)

            // Clear existing annotations and add new ones
            val existingAnnotations = mapView.annotations.filterNot {
                it === mapView.userLocation
            }
            if (existingAnnotations.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                mapView.removeAnnotations(existingAnnotations as List<MKAnnotationProtocol>)
            }

            // Add parking spot annotations
            annotationsWithSpots.forEach { (_, annotation) ->
                mapView.addAnnotation(annotation)
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { mapView ->
            mapView.delegate = null
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MiniMap(
    spot: ParkingSpot,
    modifier: Modifier
) {
    val annotation = remember(spot) {
        MKPointAnnotation().apply {
            setCoordinate(CLLocationCoordinate2DMake(spot.latitude, spot.longitude))
            setTitle(spot.name)
            setSubtitle(spot.address)
        }
    }

    UIKitView(
        factory = {
            MKMapView().apply {
                showsUserLocation = false
                setScrollEnabled(false)
                setZoomEnabled(false)
                setRotateEnabled(false)
                setPitchEnabled(false)

                // Set region centered on spot
                val center = CLLocationCoordinate2DMake(spot.latitude, spot.longitude)
                val region = MKCoordinateRegionMakeWithDistance(center, 500.0, 500.0)
                setRegion(region, animated = false)

                // Add the annotation
                addAnnotation(annotation)
            }
        },
        modifier = modifier
    )
}
