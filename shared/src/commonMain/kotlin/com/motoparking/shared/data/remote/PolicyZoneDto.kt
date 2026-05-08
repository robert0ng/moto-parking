package com.motoparking.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PolicyZoneDto(
    val id: Int,
    val city: String,
    val district: String? = null,
    val scope: String,
    val plates: List<String>,
    @SerialName("effective_date")
    val effectiveDate: String,
    @SerialName("fee_description")
    val feeDescription: String? = null,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    @SerialName("source_label")
    val sourceLabel: String? = null,
    val notes: String? = null
)
