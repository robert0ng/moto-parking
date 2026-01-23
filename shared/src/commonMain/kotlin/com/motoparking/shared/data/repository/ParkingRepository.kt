package com.motoparking.shared.data.repository

import com.motoparking.shared.data.remote.ParkingDataSource
import com.motoparking.shared.data.remote.ParkingSpotDto
import com.motoparking.shared.domain.model.ParkingSpot
import com.motoparking.shared.domain.model.PlateType

class ParkingRepository(
    private val dataSource: ParkingDataSource
) {
    // In-memory cache for parking spots to avoid redundant API calls
    private val spotCache = mutableMapOf<String, ParkingSpot>()

    /**
     * Fetch all parking spots
     */
    suspend fun getAllParkingSpots(): List<ParkingSpot> {
        val spots = dataSource.getAllParkingSpots().map { it.toDomain() }
        // Cache all fetched spots
        spots.forEach { spotCache[it.id] = it }
        return spots
    }

    /**
     * Fetch a single parking spot by ID
     * Checks cache first before making API call
     */
    suspend fun getParkingSpotById(spotId: String): ParkingSpot? {
        // Check cache first
        spotCache[spotId]?.let { return it }
        // Fallback to API
        return dataSource.getParkingSpotById(spotId)?.toDomain()?.also {
            spotCache[it.id] = it
        }
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
        val spots = dataSource.getNearbyParkingSpots(latitude, longitude, radiusMeters, offset, limit)
            .map { it.toDomain() }
        // Cache all fetched spots
        spots.forEach { spotCache[it.id] = it }
        return spots
    }

    /**
     * Search parking spots by name or address
     */
    suspend fun searchParkingSpots(query: String): List<ParkingSpot> {
        val spots = dataSource.searchParkingSpots(query).map { it.toDomain() }
        // Cache all fetched spots
        spots.forEach { spotCache[it.id] = it }
        return spots
    }

    /**
     * Clear the in-memory cache
     */
    fun clearCache() {
        spotCache.clear()
    }

    /**
     * Filter parking spots by plate type
     */
    suspend fun getParkingSpotsByPlateType(plateType: PlateType): List<ParkingSpot> {
        val spots = dataSource.getParkingSpotsByPlateType(plateType.name).map { it.toDomain() }
        // Cache all fetched spots
        spots.forEach { spotCache[it.id] = it }
        return spots
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
            val spot = response.toDomain()
            // Cache the newly created spot
            spotCache[spot.id] = spot
            Result.success(spot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user's favorite spots
     */
    suspend fun getUserFavorites(userId: String): List<ParkingSpot> {
        val spots = dataSource.getUserFavorites(userId).map { it.toDomain() }
        // Cache all fetched spots
        spots.forEach { spotCache[it.id] = it }
        return spots
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

    /**
     * Check if user can check in (24-hour cooldown)
     */
    suspend fun canUserCheckIn(userId: String, spotId: String): Boolean {
        return try {
            dataSource.canUserCheckIn(userId, spotId)
        } catch (e: Exception) {
            true // Allow check-in on error (fail open)
        }
    }
}

// Extension function to convert DTO to domain model using the mapper
private fun ParkingSpotDto.toDomain(): ParkingSpot = ParkingSpotMapper.toDomain(this)
