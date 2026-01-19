package com.motoparking.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParkingSpotDto(
    val id: String,
    val name: String,
    val address: String,
    @SerialName("plate_types")
    val plateTypes: List<String>,
    val capacity: Int? = null,
    val source: String,
    @SerialName("is_verified")
    val isVerified: Boolean = false,
    val description: String? = null,
    @SerialName("external_id")
    val externalId: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
