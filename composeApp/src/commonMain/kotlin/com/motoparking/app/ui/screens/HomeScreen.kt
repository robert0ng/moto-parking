package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.motoparking.app.ui.components.ProfileDialog
import com.motoparking.app.ui.viewmodels.AuthViewModel
import com.motoparking.app.util.DEFAULT_LOCATION
import com.motoparking.app.util.Geocoder
import com.motoparking.app.util.GoogleSignInResult
import com.motoparking.app.util.Location
import com.motoparking.app.util.LocationPermissionStatus
import com.motoparking.app.util.LocationService
import com.motoparking.app.util.RequestLocationPermission
import org.koin.compose.viewmodel.koinViewModel

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
    authViewModel: AuthViewModel = koinViewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.LIST) }
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

    // Fetch location name when location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            geocoder.getLocationName(location.latitude, location.longitude) { name ->
                locationName = name
            }
        }
    }

    // Build the toolbar title - only show location name if permission granted
    val toolbarTitle = remember(locationName, selectedRadius, permissionStatus) {
        val radiusText = formatRadius(selectedRadius)
        if (permissionStatus == LocationPermissionStatus.GRANTED && locationName != null) {
            "${locationName}附近$radiusText"
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
                currentScreen == Screen.MAP -> MapScreenPlaceholder()
                currentScreen == Screen.LIST -> {
                    val location = currentLocation ?: DEFAULT_LOCATION
                    ListScreen(
                        currentLatitude = location.latitude,
                        currentLongitude = location.longitude,
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
fun MapScreenPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "地圖功能開發中",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "將整合 Google Maps / Apple Maps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
