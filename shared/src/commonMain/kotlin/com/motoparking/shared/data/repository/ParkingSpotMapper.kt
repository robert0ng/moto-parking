package com.motoparking.shared.data.repository

import com.motoparking.shared.data.remote.ParkingSpotDto
import com.motoparking.shared.domain.model.DataSource
import com.motoparking.shared.domain.model.ParkingSpot
import com.motoparking.shared.domain.model.PlateType
import kotlinx.datetime.Instant

object ParkingSpotMapper {
    fun toDomain(dto: ParkingSpotDto): ParkingSpot {
        return ParkingSpot(
            id = dto.id,
            name = dto.name,
            address = dto.address,
            latitude = dto.latitude ?: 0.0,
            longitude = dto.longitude ?: 0.0,
            plateTypes = parsePlateTypes(dto.plateTypes),
            capacity = dto.capacity,
            source = parseDataSource(dto.source),
            isVerified = dto.isVerified,
            description = dto.description,
            createdAt = parseInstant(dto.createdAt),
            updatedAt = parseInstant(dto.updatedAt)
        )
    }

    fun parsePlateTypes(types: List<String>): List<PlateType> {
        return types.mapNotNull { type ->
            when (type.uppercase()) {
                "YELLOW" -> PlateType.YELLOW
                "RED" -> PlateType.RED
                else -> null
            }
        }
    }

    fun parseDataSource(source: String): DataSource {
        return when (source.uppercase()) {
            "GOVERNMENT" -> DataSource.GOVERNMENT
            "KML_IMPORT" -> DataSource.KML_IMPORT
            "USER_SUBMITTED" -> DataSource.USER_SUBMITTED
            else -> DataSource.USER_SUBMITTED
        }
    }

    fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Instant.DISTANT_PAST
        }
    }
}
