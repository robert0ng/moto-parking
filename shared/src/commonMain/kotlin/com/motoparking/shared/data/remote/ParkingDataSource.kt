package com.motoparking.shared.data.remote

/**
 * Interface for parking data operations.
 * Allows for easier testing by enabling mock implementations.
 */
interface ParkingDataSource {
    suspend fun getAllParkingSpots(): List<ParkingSpotDto>
    suspend fun getParkingSpotById(spotId: String): ParkingSpotDto?
    suspend fun getNearbyParkingSpots(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        offset: Int = 0,
        limit: Int = 20
    ): List<ParkingSpotDto>
    suspend fun searchParkingSpots(query: String): List<ParkingSpotDto>
    suspend fun getParkingSpotsByPlateType(plateType: String): List<ParkingSpotDto>
    suspend fun submitParkingSpot(params: Map<String, Any?>): ParkingSpotDto
    suspend fun getUserFavorites(userId: String): List<ParkingSpotDto>
    suspend fun addToFavorites(userId: String, spotId: String)
    suspend fun removeFromFavorites(userId: String, spotId: String)
}
