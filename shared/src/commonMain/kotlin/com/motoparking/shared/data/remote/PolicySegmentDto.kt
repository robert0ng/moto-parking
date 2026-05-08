package com.motoparking.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format for a row in the `policy_segments_geojson` view.
 *
 * `geometry` is either null (for the `failed` source_method rows) or a GeoJSON
 * `LineString` object whose `coordinates` are `[lng, lat]` pairs per the GeoJSON
 * spec. The DTO -> domain conversion swaps to `(lat, lng)` exactly once.
 */
@Serializable
data class PolicySegmentDto(
    val id: Int,
    @SerialName("zone_id")
    val zoneId: Int,
    val city: String,
    val district: String? = null,
    val scope: String,
    val plates: List<String>,
    @SerialName("effective_date")
    val effectiveDate: String,
    @SerialName("fee_description")
    val feeDescription: String? = null,
    @SerialName("zone_source_url")
    val zoneSourceUrl: String? = null,
    @SerialName("zone_source_label")
    val zoneSourceLabel: String? = null,
    @SerialName("road_name")
    val roadName: String,
    @SerialName("from_road")
    val fromRoad: String? = null,
    @SerialName("to_road")
    val toRoad: String? = null,
    @SerialName("source_method")
    val sourceMethod: String,
    @SerialName("segment_source_label")
    val segmentSourceLabel: String? = null,
    val notes: String? = null,
    val geometry: GeoJsonLineStringDto? = null
)

@Serializable
data class GeoJsonLineStringDto(
    val type: String,
    /**
     * GeoJSON convention: each coordinate is `[lng, lat]`. Convert to `(lat, lng)`
     * at the DTO -> domain boundary.
     */
    val coordinates: List<List<Double>>
)
