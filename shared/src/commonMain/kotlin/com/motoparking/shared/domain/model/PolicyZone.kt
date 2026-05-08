package com.motoparking.shared.domain.model

import kotlinx.datetime.LocalDate

data class PolicyZone(
    val id: Int,
    val city: String,
    val district: String?,
    val scope: String,
    val plates: List<PlateType>,
    val effectiveDate: LocalDate,
    val feeDescription: String?,
    val sourceUrl: String?,
    val sourceLabel: String?,
    val notes: String?
)
