package com.motoparking.shared.domain.model

import kotlinx.datetime.LocalDate

/**
 * Provenance / quality classification of a [PolicySegment]'s geometry.
 *
 * Map rendering style:
 * - [OSM_TRIMMED], [OSM_WHOLE], [MANUAL]: solid bold polyline (real road shape).
 * - [STRAIGHT_LINE]: dashed, ~50% opacity (endpoints right, shape approximate).
 * - [FAILED]: do not draw on map; surface as a name-only list entry.
 */
enum class SegmentSourceMethod {
    OSM_TRIMMED,
    OSM_WHOLE,
    MANUAL,
    STRAIGHT_LINE,
    FAILED,
    UNKNOWN;

    companion object {
        fun parse(raw: String): SegmentSourceMethod = when (raw.lowercase()) {
            "osm-trimmed" -> OSM_TRIMMED
            "osm-whole" -> OSM_WHOLE
            "manual" -> MANUAL
            "straight-line" -> STRAIGHT_LINE
            "failed" -> FAILED
            else -> UNKNOWN
        }
    }
}

/**
 * A 2-D coordinate in `(latitude, longitude)` order, matching the rest of the
 * codebase's convention (Google Maps `LatLng`, MapKit `CLLocationCoordinate2D`).
 */
data class LatLng(val latitude: Double, val longitude: Double)

/**
 * A named road segment that falls within a [PolicyZone] and to which the zone's
 * plate-policy applies (e.g. one of the road segments in 勤美草悟道 商圈 in Taichung).
 *
 * `geometry` is null only for [SegmentSourceMethod.FAILED] segments, which carry
 * enough metadata to render as a list entry but cannot be drawn on the map.
 */
data class PolicySegment(
    val id: Int,
    val zoneId: Int,
    val city: String,
    val district: String?,
    val scope: String,
    val plates: List<PlateType>,
    val effectiveDate: LocalDate,
    val feeDescription: String?,
    val zoneSourceUrl: String?,
    val zoneSourceLabel: String?,
    val roadName: String,
    val fromRoad: String?,
    val toRoad: String?,
    val sourceMethod: SegmentSourceMethod,
    val segmentSourceLabel: String?,
    val notes: String?,
    val geometry: List<LatLng>?
)
