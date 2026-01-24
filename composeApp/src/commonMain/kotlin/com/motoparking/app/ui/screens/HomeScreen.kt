package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.motoparking.app.ui.components.ProfileDialog
import com.motoparking.app.ui.viewmodels.AuthViewModel
import com.motoparking.app.ui.viewmodels.HomeViewModel
import com.motoparking.app.util.DEFAULT_LOCATION
import com.motoparking.app.util.Geocoder
import com.motoparking.app.util.GeoUtils
import com.motoparking.app.util.GoogleSignInResult
import com.motoparking.app.util.Location
import com.motoparking.app.util.LocationPermissionStatus
import com.motoparking.app.util.LocationService
import com.motoparking.app.util.RequestLocationPermission
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// Threshold distance (in meters) for showing "Search this area" button
private const val SEARCH_AREA_THRESHOLD_METERS = 500.0

enum class Screen {
    MAP, LIST
}

private fun formatRadius(meters: Int): String = when (meters) {
    500 -> "500公尺"
    1000 -> "1公里"
    2000 -> "2公里"
    5000 -> "5公里"
    else -> "${meters}m"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSpotClick: (spotId: String) -> Unit = {},
    authViewModel: AuthViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel()
) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.MAP) }
    var selectedRadius by remember { mutableStateOf(1000) }
    var showRadiusMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Auth state
    val authState by authViewModel.uiState.collectAsState()

    // Location state
    val locationService = remember { LocationService() }
    val geocoder = remember { Geocoder() }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationName by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoadingLocation by remember { mutableStateOf(true) }
    var permissionStatus by remember { mutableStateOf<LocationPermissionStatus?>(null) }
    var shouldRequestPermission by remember { mutableStateOf(true) }

    // Search location - persisted via HomeViewModel
    val searchLocationState by homeViewModel.searchLocation.collectAsState()
    val searchLocation = remember(searchLocationState) {
        if (searchLocationState.latitude != null && searchLocationState.longitude != null) {
            Location(searchLocationState.latitude!!, searchLocationState.longitude!!)
        } else null
    }
    val searchLocationName = searchLocationState.name

    // When currentLocation first loads AND no persisted location, use current location
    LaunchedEffect(currentLocation, searchLocationState) {
        if (searchLocation == null && currentLocation != null) {
            homeViewModel.updateSearchLocation(
                currentLocation!!.latitude,
                currentLocation!!.longitude
            )
        }
    }

    // Fetch search location name when searchLocation changes and name is not set
    LaunchedEffect(searchLocation) {
        searchLocation?.let { location ->
            if (searchLocationState.name == null) {
                geocoder.getLocationName(location.latitude, location.longitude) { name ->
                    homeViewModel.updateLocationName(name)
                }
            }
        }
    }

    // Fetch location name when location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            geocoder.getLocationName(location.latitude, location.longitude) { name ->
                locationName = name
            }
        }
    }

    // Build the toolbar title - show search location name (not GPS location)
    val toolbarTitle = remember(searchLocationName, selectedRadius, permissionStatus) {
        val radiusText = formatRadius(selectedRadius)
        if (permissionStatus == LocationPermissionStatus.GRANTED && searchLocationName != null) {
            "${searchLocationName}附近$radiusText"
        } else {
            "附近$radiusText"
        }
    }

    // Function to get location after permission is determined
    fun getLocationIfPermitted() {
        when (permissionStatus) {
            LocationPermissionStatus.GRANTED -> {
                isLoadingLocation = true
                locationService.getCurrentLocation(
                    onSuccess = { location ->
                        currentLocation = location
                        isLoadingLocation = false
                    },
                    onError = { error ->
                        locationError = error
                        currentLocation = DEFAULT_LOCATION
                        isLoadingLocation = false
                    }
                )
            }
            LocationPermissionStatus.DENIED, LocationPermissionStatus.NOT_DETERMINED -> {
                currentLocation = DEFAULT_LOCATION
                isLoadingLocation = false
            }
            null -> {
                // Permission not yet checked
            }
        }
    }

    // Request permission on first launch
    if (shouldRequestPermission) {
        RequestLocationPermission { status ->
            permissionStatus = status
            shouldRequestPermission = false
            getLocationIfPermitted()
        }
    }

    // Check location permission on every app resume (in case user changed it in Settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-check permission status (user might have changed in Settings)
                val newStatus = locationService.checkPermissionStatus()
                if (newStatus != permissionStatus) {
                    permissionStatus = newStatus
                    getLocationIfPermitted()
                } else if (newStatus == LocationPermissionStatus.GRANTED && currentLocation == DEFAULT_LOCATION) {
                    // Permission was granted but we're still on default location
                    getLocationIfPermitted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val radiusOptions = listOf(500, 1000, 2000, 5000)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(toolbarTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showRadiusMenu = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "選擇範圍")
                        }
                        DropdownMenu(
                            expanded = showRadiusMenu,
                            onDismissRequest = { showRadiusMenu = false }
                        ) {
                            radiusOptions.forEach { radius ->
                                DropdownMenuItem(
                                    text = { Text(formatRadius(radius)) },
                                    onClick = {
                                        selectedRadius = radius
                                        showRadiusMenu = false
                                    },
                                    leadingIcon = if (radius == selectedRadius) {
                                        { Icon(Icons.Default.TwoWheeler, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }
                    // Profile button
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "個人資料",
                            tint = if (authState.isAuthenticated)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "地圖") },
                    label = { Text("地圖") },
                    selected = currentScreen == Screen.MAP,
                    onClick = { currentScreen = Screen.MAP }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "列表") },
                    label = { Text("列表") },
                    selected = currentScreen == Screen.LIST,
                    onClick = { currentScreen = Screen.LIST }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoadingLocation -> {
                    // Show loading while getting location
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "正在取得位置...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                currentScreen == Screen.MAP -> {
                    // Use searchLocation for search center, fallback to currentLocation then default
                    val searchCenter = searchLocation ?: currentLocation ?: DEFAULT_LOCATION
                    MapScreenContent(
                        searchLatitude = searchCenter.latitude,
                        searchLongitude = searchCenter.longitude,
                        userLatitude = currentLocation?.latitude,
                        userLongitude = currentLocation?.longitude,
                        radiusMeters = selectedRadius,
                        onSpotClick = onSpotClick,
                        onSearchArea = { lat, lon ->
                            homeViewModel.updateSearchLocation(lat, lon)
                            geocoder.getLocationName(lat, lon) { name ->
                                homeViewModel.updateLocationName(name)
                            }
                        }
                    )
                }
                currentScreen == Screen.LIST -> {
                    // Use searchLocation for list, same as map
                    val searchCenter = searchLocation ?: currentLocation ?: DEFAULT_LOCATION
                    ListScreen(
                        currentLatitude = searchCenter.latitude,
                        currentLongitude = searchCenter.longitude,
                        radiusMeters = selectedRadius,
                        onSpotClick = onSpotClick
                    )
                }
            }
        }
    }

    // Profile Dialog
    if (showProfileDialog) {
        ProfileDialog(
            authState = authState,
            onGoogleSignInResult = { result ->
                when (result) {
                    is GoogleSignInResult.Success -> {
                        authViewModel.signInWithGoogle(result.idToken, result.accessToken)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("登入成功")
                        }
                    }
                    is GoogleSignInResult.Error -> {
                        // Reopen dialog to show error
                        showProfileDialog = true
                    }
                    is GoogleSignInResult.Cancelled -> {
                        // User cancelled, do nothing
                    }
                }
            },
            onGoogleSignInStarted = {
                // Dismiss dialog when sign-in starts to avoid UI overlap
                showProfileDialog = false
            },
            onSignOut = { authViewModel.signOut() },
            onDismiss = {
                showProfileDialog = false
                authViewModel.clearError()
            }
        )
    }
}

@Composable
fun MapScreenContent(
    viewModel: ParkingListViewModel = koinViewModel(),
    searchLatitude: Double = 25.048,
    searchLongitude: Double = 121.517,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    radiusMeters: Int = 1000,
    onSpotClick: (spotId: String) -> Unit = {},
    onSearchArea: ((latitude: Double, longitude: Double) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Track current map center for "Search This Area" button
    var mapCenterLatitude by remember { mutableStateOf(searchLatitude) }
    var mapCenterLongitude by remember { mutableStateOf(searchLongitude) }

    // Calculate distance from search location to map center
    val distanceFromSearchLocation = remember(searchLatitude, searchLongitude, mapCenterLatitude, mapCenterLongitude) {
        GeoUtils.distanceInMeters(searchLatitude, searchLongitude, mapCenterLatitude, mapCenterLongitude)
    }

    // Show "Search This Area" button when map center is far from search location
    val showSearchAreaButton = distanceFromSearchLocation > SEARCH_AREA_THRESHOLD_METERS

    // Load nearby spots when search location/radius changes
    LaunchedEffect(searchLatitude, searchLongitude, radiusMeters) {
        viewModel.loadNearbySpots(searchLatitude, searchLongitude, radiusMeters)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Always show MapScreen to preserve camera position
        // Pass search location for initial camera position
        MapScreen(
            parkingSpots = uiState.spots,
            userLatitude = searchLatitude,
            userLongitude = searchLongitude,
            selectedRadius = radiusMeters,
            onSpotClick = { spot -> onSpotClick(spot.id) },
            onMapCenterChanged = { lat, lon ->
                mapCenterLatitude = lat
                mapCenterLongitude = lon
            }
        )

        // "Search This Area" button (overlay)
        if (showSearchAreaButton && onSearchArea != null && !uiState.isLoading) {
            Button(
                onClick = { onSearchArea(mapCenterLatitude, mapCenterLongitude) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("搜尋此區域")
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Error overlay
        if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.loadNearbySpots(searchLatitude, searchLongitude, radiusMeters)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("載入失敗，點擊重試")
                }
            }
        }
    }
}
