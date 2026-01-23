package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import com.motoparking.shared.domain.model.ParkingSpot
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIColor
import platform.darwin.NSObject

// Default location: Taipei Main Station
private const val DEFAULT_LATITUDE = 25.0478
private const val DEFAULT_LONGITUDE = 121.5170
private const val DEFAULT_SPAN_METERS = 1000.0

/**
 * MKMapView delegate to track map region changes.
 */
@OptIn(ExperimentalForeignApi::class)
private class MapViewDelegate(
    private val onRegionChanged: (latitude: Double, longitude: Double) -> Unit
) : NSObject(), MKMapViewDelegateProtocol {
    // Flag to track if the region change is programmatic (from setRegion)
    var isProgrammaticChange = false

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        if (!isProgrammaticChange) {
            val center = mapView.centerCoordinate
            center.useContents {
                onRegionChanged(latitude, longitude)
            }
        }
        // Reset the flag after the region change completes
        isProgrammaticChange = false
    }
}

/**
 * Holder to keep both map view and delegate references together,
 * preventing the delegate from being garbage collected.
 */
private data class MapViewHolder(
    val mapView: MKMapView,
    val delegate: MapViewDelegate?
)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapScreen(
    parkingSpots: List<ParkingSpot>,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedRadius: Int,
    onSpotClick: (ParkingSpot) -> Unit,
    onMapCenterChanged: ((latitude: Double, longitude: Double) -> Unit)?
) {
    val centerLatitude = userLatitude ?: DEFAULT_LATITUDE
    val centerLongitude = userLongitude ?: DEFAULT_LONGITUDE
    val spanMeters = selectedRadius.toDouble().coerceAtLeast(DEFAULT_SPAN_METERS)

    // Store reference to map view and delegate together to prevent delegate GC
    val mapViewHolder = remember { mutableStateOf<MapViewHolder?>(null) }

    // Create delegate for tracking map region changes
    val mapDelegate = remember(onMapCenterChanged) {
        onMapCenterChanged?.let { callback ->
            MapViewDelegate { lat, lon -> callback(lat, lon) }
        }
    }

    // Create annotations from parking spots (use distinctBy to avoid duplicates)
    val annotationsWithSpots = remember(parkingSpots) {
        parkingSpots.distinctBy { it.id }.map { spot ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        UIKitView(
            factory = {
                MKMapView().apply {
                    showsUserLocation = true

                    // Set delegate for tracking region changes
                    mapDelegate?.let { delegate = it }

                    // Mark as programmatic change and set initial region
                    mapDelegate?.isProgrammaticChange = true
                    val center = CLLocationCoordinate2DMake(centerLatitude, centerLongitude)
                    val region = MKCoordinateRegionMakeWithDistance(center, spanMeters * 2, spanMeters * 2)
                    setRegion(region, animated = false)
                }.also { mapView ->
                    // Store both map view and delegate together to prevent delegate GC
                    mapViewHolder.value = MapViewHolder(mapView, mapDelegate)
                }
            },
            update = { mapView ->
                // Note: We intentionally do NOT update the region here.
                // The user controls the map position by panning.
                // They can use "Search this area" to search where they've panned to,
                // or use the recenter button to go back to their location.

                // Only update annotations when spots change
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

        // Recenter on user location button
        FloatingActionButton(
            onClick = {
                mapViewHolder.value?.let { holder ->
                    val mapView = holder.mapView
                    // userLocation can be null if location services are disabled
                    val userLocation = mapView.userLocation ?: return@let
                    val userCoord = userLocation.coordinate
                    // Only recenter if we have valid user location
                    if (userCoord.useContents { latitude != 0.0 || longitude != 0.0 }) {
                        // Mark as programmatic change (recenter doesn't trigger search)
                        holder.delegate?.isProgrammaticChange = true
                        val region = MKCoordinateRegionMakeWithDistance(userCoord, DEFAULT_SPAN_METERS, DEFAULT_SPAN_METERS)
                        mapView.setRegion(region, animated = true)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "回到目前位置"
            )
        }
    }
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
        // Note: MKMapView handles its own cleanup on deallocation - no onRelease needed
    )
}
