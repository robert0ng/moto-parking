package com.motoparking.app.ui.screens

import com.motoparking.shared.data.remote.ParkingDataSource
import com.motoparking.shared.data.remote.ParkingSpotDto
import com.motoparking.shared.data.repository.ParkingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParkingListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ParkingListViewModel
    private lateinit var fakeDataSource: FakeParkingDataSource

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDataSource = FakeParkingDataSource()
        val repository = ParkingRepository(fakeDataSource)
        viewModel = ParkingListViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadMore_deduplicatesBySpotId() = runTest(testDispatcher) {
        // Given: Initial spots loaded
        fakeDataSource.spotsToReturn = listOf(
            createSpotDto("spot-1", "Spot 1"),
            createSpotDto("spot-2", "Spot 2"),
            createSpotDto("spot-3", "Spot 3")
        )
        viewModel.loadNearbySpots(25.0, 121.5)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.spots.size)

        // When: Load more returns overlapping spots (spot-2 and spot-3 are duplicates)
        fakeDataSource.spotsToReturn = listOf(
            createSpotDto("spot-2", "Spot 2"),  // Duplicate
            createSpotDto("spot-3", "Spot 3"),  // Duplicate
            createSpotDto("spot-4", "Spot 4"),
            createSpotDto("spot-5", "Spot 5")
        )
        viewModel.loadMore()
        advanceUntilIdle()

        // Then: Duplicates are removed, only unique spots remain
        val spots = viewModel.uiState.value.spots
        assertEquals(5, spots.size, "Should have 5 unique spots after deduplication")

        val spotIds = spots.map { it.id }
        assertEquals(spotIds.distinct().size, spotIds.size, "All spot IDs should be unique")
        assertTrue(spotIds.contains("spot-1"))
        assertTrue(spotIds.contains("spot-2"))
        assertTrue(spotIds.contains("spot-3"))
        assertTrue(spotIds.contains("spot-4"))
        assertTrue(spotIds.contains("spot-5"))
    }

    @Test
    fun loadMore_withAllDuplicates_doesNotAddNewSpots() = runTest(testDispatcher) {
        // Given: Initial spots
        fakeDataSource.spotsToReturn = listOf(
            createSpotDto("spot-1", "Spot 1"),
            createSpotDto("spot-2", "Spot 2")
        )
        viewModel.loadNearbySpots(25.0, 121.5)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.spots.size)

        // When: Load more returns only duplicates
        fakeDataSource.spotsToReturn = listOf(
            createSpotDto("spot-1", "Spot 1"),
            createSpotDto("spot-2", "Spot 2")
        )
        viewModel.loadMore()
        advanceUntilIdle()

        // Then: No new spots added
        assertEquals(2, viewModel.uiState.value.spots.size)
    }

    @Test
    fun loadMore_preservesOrderWithFirstOccurrence() = runTest(testDispatcher) {
        // Given: Initial spots
        fakeDataSource.spotsToReturn = listOf(
            createSpotDto("spot-1", "Original Spot 1"),
            createSpotDto("spot-2", "Original Spot 2")
        )
        viewModel.loadNearbySpots(25.0, 121.5)
        advanceUntilIdle()

        // When: Load more with duplicate that has different name (same ID)
        fakeDataSource.spotsToReturn = listOf(
            createSpotDto("spot-2", "Updated Spot 2"),  // Same ID, different name
            createSpotDto("spot-3", "Spot 3")
        )
        viewModel.loadMore()
        advanceUntilIdle()

        // Then: First occurrence is preserved (distinctBy keeps first)
        val spots = viewModel.uiState.value.spots
        assertEquals(3, spots.size)
        assertEquals("Original Spot 2", spots.find { it.id == "spot-2" }?.name)
    }

    private fun createSpotDto(id: String, name: String) = ParkingSpotDto(
        id = id,
        name = name,
        address = "Test Address",
        latitude = 25.0,
        longitude = 121.5,
        plate_types = "YELLOW",
        capacity = null,
        source = "GOVERNMENT",
        is_verified = false,
        description = null,
        created_at = "2024-01-01T00:00:00Z",
        updated_at = "2024-01-01T00:00:00Z"
    )
}

/**
 * Fake data source for testing
 */
class FakeParkingDataSource : ParkingDataSource {
    var spotsToReturn: List<ParkingSpotDto> = emptyList()

    override suspend fun getAllParkingSpots(): List<ParkingSpotDto> = spotsToReturn
    override suspend fun getParkingSpotById(spotId: String): ParkingSpotDto? =
        spotsToReturn.find { it.id == spotId }
    override suspend fun getNearbyParkingSpots(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        offset: Int,
        limit: Int
    ): List<ParkingSpotDto> = spotsToReturn
    override suspend fun searchParkingSpots(query: String): List<ParkingSpotDto> = spotsToReturn
    override suspend fun getParkingSpotsByPlateType(plateType: String): List<ParkingSpotDto> = spotsToReturn
    override suspend fun submitParkingSpot(params: Map<String, Any?>): ParkingSpotDto =
        throw NotImplementedError("Not needed for this test")
    override suspend fun getUserFavorites(userId: String): List<ParkingSpotDto> = emptyList()
    override suspend fun addToFavorites(userId: String, spotId: String) {}
    override suspend fun removeFromFavorites(userId: String, spotId: String) {}
    override suspend fun isFavorite(userId: String, spotId: String): Boolean = false
    override suspend fun submitReport(userId: String, spotId: String, category: String, comment: String?) {}
    override suspend fun checkIn(userId: String, spotId: String) {}
    override suspend fun getCheckInCount(spotId: String): Int = 0
    override suspend fun canUserCheckIn(userId: String, spotId: String): Boolean = true
}
