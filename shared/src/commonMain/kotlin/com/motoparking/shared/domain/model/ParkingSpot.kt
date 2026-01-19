package com.motoparking.shared.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ParkingSpot(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val plateTypes: List<PlateType>,
    val capacity: Int? = null,
    val source: DataSource,
    val isVerified: Boolean = false,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
enum class PlateType {
    YELLOW,  // 黃牌 - 大型重型機車 (250cc-550cc)
    RED      // 紅牌 - 大型重型機車 (550cc+)
}

@Serializable
enum class DataSource {
    GOVERNMENT,      // From TDX or other government sources
    KML_IMPORT,      // Imported from Google Maps KML
    USER_SUBMITTED   // Submitted by app users
}

// Extension for display
fun PlateType.displayName(): String = when (this) {
    PlateType.YELLOW -> "黃牌"
    PlateType.RED -> "紅牌"
}

fun DataSource.displayName(): String = when (this) {
    DataSource.GOVERNMENT -> "政府資料"
    DataSource.KML_IMPORT -> "社群資料"
    DataSource.USER_SUBMITTED -> "使用者提交"
}
