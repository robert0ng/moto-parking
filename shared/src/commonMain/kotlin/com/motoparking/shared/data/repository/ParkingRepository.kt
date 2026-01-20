package com.motoparking.shared.data.repository

import com.motoparking.shared.data.remote.ParkingDataSource
import com.motoparking.shared.data.remote.ParkingSpotDto
import com.motoparking.shared.domain.model.ParkingSpot
import com.motoparking.shared.domain.model.PlateType

class ParkingRepository(
    private val dataSource: ParkingDataSource
) {
    /**
     * Fetch all parking spots
     */
    suspend fun getAllParkingSpots(): List<ParkingSpot> {
        return dataSource.getAllParkingSpots().map { it.toDomain() }
    }

    /**
     * Fetch a single parking spot by ID
     */
    suspend fun getParkingSpotById(spotId: String): ParkingSpot? {
        return dataSource.getParkingSpotById(spotId)?.toDomain()
    }

    /**
     * Fetch nearby parking spots using PostGIS with pagination
     */
    suspend fun getNearbyParkingSpots(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 1000,
        offset: Int = 0,
        limit: Int = 20
    ): List<ParkingSpot> {
        return dataSource.getNearbyParkingSpots(latitude, longitude, radiusMeters, offset, limit)
            .map { it.toDomain() }
    }

    /**
     * Search parking spots by name or address
     */
    suspend fun searchParkingSpots(query: String): List<ParkingSpot> {
        return dataSource.searchParkingSpots(query).map { it.toDomain() }
    }

    /**
     * Filter parking spots by plate type
     */
    suspend fun getParkingSpotsByPlateType(plateType: PlateType): List<ParkingSpot> {
        return dataSource.getParkingSpotsByPlateType(plateType.name).map { it.toDomain() }
    }

    /**
     * Submit a new parking spot
     */
    suspend fun submitParkingSpot(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        plateTypes: List<PlateType>,
        capacity: Int?,
        description: String?
    ): Result<ParkingSpot> {
        return try {
            val params = mapOf(
                "p_name" to name,
                "p_address" to address,
                "p_latitude" to latitude,
                "p_longitude" to longitude,
                "p_plate_types" to plateTypes.joinToString(",") { it.name },
                "p_capacity" to capacity,
                "p_description" to description
            )
            val response = dataSource.submitParkingSpot(params)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user's favorite spots
     */
    suspend fun getUserFavorites(userId: String): List<ParkingSpot> {
        return dataSource.getUserFavorites(userId).map { it.toDomain() }
    }

    /**
     * Add spot to favorites
     */
    suspend fun addToFavorites(userId: String, spotId: String): Result<Unit> {
        return try {
            dataSource.addToFavorites(userId, spotId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove spot from favorites
     */
    suspend fun removeFromFavorites(userId: String, spotId: String): Result<Unit> {
        return try {
            dataSource.removeFromFavorites(userId, spotId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if spot is in user's favorites
     */
    suspend fun isFavorite(userId: String, spotId: String): Boolean {
        return try {
            dataSource.isFavorite(userId, spotId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Submit a report for a parking spot
     */
    suspend fun submitReport(userId: String, spotId: String, category: String, comment: String?): Result<Unit> {
        return try {
            dataSource.submitReport(userId, spotId, category, comment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check in to a parking spot
     */
    suspend fun checkIn(userId: String, spotId: String): Result<Unit> {
        return try {
            dataSource.checkIn(userId, spotId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total check-in count for a spot
     */
    suspend fun getCheckInCount(spotId: String): Int {
        return try {
            dataSource.getCheckInCount(spotId)
        } catch (e: Exception) {
            0
        }
    }
}

// Extension function to convert DTO to domain model using the mapper
private fun ParkingSpotDto.toDomain(): ParkingSpot = ParkingSpotMapper.toDomain(this)
