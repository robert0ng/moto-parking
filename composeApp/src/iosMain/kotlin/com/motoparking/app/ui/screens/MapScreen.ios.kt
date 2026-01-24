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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import com.motoparking.shared.domain.model.ParkingSpot
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKUserLocation
import platform.UIKit.UIColor
import platform.darwin.NSObject

// Default location: Taipei Main Station
private const val DEFAULT_LATITUDE = 25.0478
private const val DEFAULT_LONGITUDE = 121.5170
private const val DEFAULT_SPAN_METERS = 1000.0

/**
 * MKMapView delegate to track map region changes and annotation selection.
 */
@OptIn(ExperimentalForeignApi::class)
private class MapViewDelegate(
    private val onRegionChanged: (latitude: Double, longitude: Double) -> Unit,
    private val onAnnotationSelected: ((MKAnnotationProtocol, MKAnnotationView, MKMapView) -> Unit)? = null
) : NSObject(), MKMapViewDelegateProtocol {
    // Flag to track if the region change is programmatic (from setRegion)
    var isProgrammaticChange = false
    // Track the currently selected spot ID for two-step interaction (using ID instead of annotation reference for stability)
    var currentlySelectedSpotId: String? = null
    // Also track the coordinate for visual styling (coordinates are values, not references)
    var currentlySelectedCoordinate: Pair<Double, Double>? = null

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

    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation ?: return
        // Ignore user location annotation
        if (annotation is MKUserLocation) return
        onAnnotationSelected?.invoke(annotation, didSelectAnnotationView, mapView)
    }

    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        // Use default view for user location
        if (viewForAnnotation is MKUserLocation) return null

        val identifier = "ParkingSpotMarker"
        val annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier) as? MKMarkerAnnotationView
            ?: MKMarkerAnnotationView(viewForAnnotation, identifier)

        annotationView.annotation = viewForAnnotation
        annotationView.canShowCallout = true
        annotationView.markerTintColor = UIColor.redColor

        return annotationView
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

    // Store spot lookup by coordinate (coordinates are values, stable across recomposition)
    val spotByCoordinate = remember(parkingSpots) {
        parkingSpots.distinctBy { it.id }.associate { spot ->
            (spot.latitude to spot.longitude) to spot
        }
    }

    // Use rememberUpdatedState to keep callbacks current without recreating the delegate
    // This fixes the issue where delegate recreation causes stale references
    val currentOnMapCenterChanged by rememberUpdatedState(onMapCenterChanged)
    val currentOnSpotClick by rememberUpdatedState(onSpotClick)
    val currentSpotByCoordinate by rememberUpdatedState(spotByCoordinate)

    // Create a STABLE delegate (not recreated when dependencies change)
    // The delegate uses rememberUpdatedState references which always point to current values
    val mapDelegate = remember {
        var delegate: MapViewDelegate? = null
        delegate = MapViewDelegate(
            onRegionChanged = { lat, lon ->
                currentOnMapCenterChanged?.invoke(lat, lon)
            },
            onAnnotationSelected = { annotation, annotationView, mapView ->
                val del = delegate ?: return@MapViewDelegate

                // Get coordinate from annotation (coordinates are values, stable across recomposition)
                val coord = annotation.coordinate.useContents { latitude to longitude }

                // Look up spot by coordinate
                val spot = currentSpotByCoordinate[coord] ?: return@MapViewDelegate

                if (del.currentlySelectedSpotId == spot.id) {
                    // Second tap on already selected spot - navigate to detail
                    currentOnSpotClick(spot)
                    // Clear selection state
                    del.currentlySelectedSpotId = null
                    del.currentlySelectedCoordinate = null
                } else {
                    // First tap - select this spot and center map on it
                    del.currentlySelectedSpotId = spot.id
                    del.currentlySelectedCoordinate = coord

                    // Center map on the selected spot
                    del.isProgrammaticChange = true
                    val coordinate = CLLocationCoordinate2DMake(spot.latitude, spot.longitude)
                    val region = MKCoordinateRegionMakeWithDistance(coordinate, DEFAULT_SPAN_METERS, DEFAULT_SPAN_METERS)
                    mapView.setRegion(region, animated = true)
                }

                // Always deselect so the annotation can be tapped again
                // (didSelectAnnotationView only fires for non-selected annotations)
                mapView.deselectAnnotation(annotation, animated = false)
            }
        )
        delegate
    }

    Box(modifier = Modifier.fillMaxSize()) {
        UIKitView(
            factory = {
                MKMapView().apply {
                    showsUserLocation = true

                    // Set delegate for tracking region changes and annotation clicks
                    delegate = mapDelegate

                    // Mark as programmatic change and set initial region
                    mapDelegate.isProgrammaticChange = true
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

                // Get current annotations on map (excluding user location)
                val existingAnnotations = mapView.annotations.filterNot {
                    it === mapView.userLocation
                }

                // Get the set of annotations we want to display
                val desiredAnnotations = annotationsWithSpots.map { it.second }.toSet()

                // Only update if annotations have actually changed
                // Compare by checking if the sets contain the same objects
                @Suppress("UNCHECKED_CAST")
                val existingSet = existingAnnotations.toSet() as Set<MKAnnotationProtocol>
                if (existingSet != desiredAnnotations) {
                    // Remove old annotations
                    if (existingAnnotations.isNotEmpty()) {
                        mapView.removeAnnotations(existingAnnotations as List<MKAnnotationProtocol>)
                    }
                    // Add new annotations
                    annotationsWithSpots.forEach { (_, annotation) ->
                        mapView.addAnnotation(annotation)
                    }
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
