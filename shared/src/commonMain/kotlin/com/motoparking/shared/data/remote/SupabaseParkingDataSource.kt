package com.motoparking.shared.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase implementation of ParkingDataSource
 */
class SupabaseParkingDataSource(
    private val supabaseClient: SupabaseClient
) : ParkingDataSource {

    private val selectColumns = """
        id, name, address, plate_types, capacity, source,
        is_verified, description, created_at, updated_at
    """.trimIndent()

    override suspend fun getAllParkingSpots(): List<ParkingSpotDto> {
        return supabaseClient
            .from("parking_spots")
            .select(Columns.raw(selectColumns))
            .decodeList()
    }

    override suspend fun getParkingSpotById(spotId: String): ParkingSpotDto? {
        return supabaseClient
            .from("parking_spots")
            .select(Columns.raw(selectColumns)) {
                filter {
                    eq("id", spotId)
                }
            }
            .decodeSingleOrNull()
    }

    override suspend fun getNearbyParkingSpots(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        offset: Int,
        limit: Int
    ): List<ParkingSpotDto> {
        val params = buildJsonObject {
            put("lat", latitude)
            put("lng", longitude)
            put("radius_meters", radiusMeters)
            put("result_offset", offset)
            put("result_limit", limit)
        }
        return supabaseClient.postgrest.rpc(
            function = "get_nearby_spots",
            parameters = params
        ).decodeList()
    }

    override suspend fun searchParkingSpots(query: String): List<ParkingSpotDto> {
        return supabaseClient
            .from("parking_spots")
            .select(Columns.raw(selectColumns)) {
                filter {
                    or {
                        ilike("name", "%$query%")
                        ilike("address", "%$query%")
                    }
                }
            }
            .decodeList()
    }

    override suspend fun getParkingSpotsByPlateType(plateType: String): List<ParkingSpotDto> {
        return supabaseClient
            .from("parking_spots")
            .select(Columns.raw(selectColumns)) {
                filter {
                    contains("plate_types", listOf(plateType))
                }
            }
            .decodeList()
    }

    override suspend fun submitParkingSpot(params: Map<String, Any?>): ParkingSpotDto {
        val jsonParams = buildJsonObject {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Int -> put(key, value)
                    is Double -> put(key, value)
                    is Boolean -> put(key, value)
                    null -> { /* skip null values */ }
                }
            }
        }
        return supabaseClient.postgrest.rpc(
            function = "insert_parking_spot",
            parameters = jsonParams
        ).decodeSingle()
    }

    override suspend fun getUserFavorites(userId: String): List<ParkingSpotDto> {
        val response = supabaseClient
            .from("user_favorites")
            .select(Columns.raw("""
                parking_spots($selectColumns)
            """.trimIndent())) {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<FavoriteWithSpot>()

        return response.mapNotNull { it.parkingSpots }
    }

    override suspend fun addToFavorites(userId: String, spotId: String) {
        supabaseClient
            .from("user_favorites")
            .insert(mapOf("user_id" to userId, "spot_id" to spotId))
    }

    override suspend fun removeFromFavorites(userId: String, spotId: String) {
        supabaseClient
            .from("user_favorites")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("spot_id", spotId)
                }
            }
    }
}

@Serializable
private data class FavoriteWithSpot(
    @kotlinx.serialization.SerialName("parking_spots")
    val parkingSpots: ParkingSpotDto?
)
