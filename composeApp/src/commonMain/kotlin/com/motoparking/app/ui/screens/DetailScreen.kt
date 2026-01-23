package com.motoparking.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motoparking.app.ui.components.ProfileDialog
import com.motoparking.app.ui.components.ReportDialog
import com.motoparking.app.ui.viewmodels.AuthViewModel
import com.motoparking.app.ui.viewmodels.DetailViewModel
import com.motoparking.app.util.GoogleSignInResult
import com.motoparking.app.util.openInMaps
import com.motoparking.shared.domain.model.DataSource
import com.motoparking.shared.domain.model.ParkingSpot
import com.motoparking.shared.domain.model.PlateType
import com.motoparking.shared.domain.model.displayName
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    spotId: String,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    // Show login dialog when auth is required
    var showLoginDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(spotId) {
        viewModel.loadSpot(spotId)
    }

    // Close report dialog on success
    LaunchedEffect(uiState.reportSuccess) {
        if (uiState.reportSuccess) {
            showReportDialog = false
            viewModel.clearReportState()
        }
    }

    // When requiresAuth changes to true, show login dialog
    LaunchedEffect(uiState.requiresAuth) {
        if (uiState.requiresAuth) {
            showLoginDialog = true
            viewModel.clearAuthRequired()
        }
    }

    // Refresh favorite status when auth state changes
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            viewModel.refreshFavoriteStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.spot?.name ?: "停車位詳情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Favorite button
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        enabled = !uiState.isFavoriteLoading
                    ) {
                        if (uiState.isFavoriteLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (uiState.isFavorite) "移除收藏" else "加入收藏"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadSpot(spotId) }
                    )
                }
                uiState.spot != null -> {
                    val spot = uiState.spot!!
                    SpotDetailContent(
                        spot = spot,
                        onOpenMaps = {
                            openInMaps(
                                latitude = spot.latitude,
                                longitude = spot.longitude,
                                label = spot.name
                            )
                        },
                        onReport = {
                            if (viewModel.isAuthenticated()) {
                                showReportDialog = true
                            } else {
                                showLoginDialog = true
                            }
                        },
                        onCheckIn = { viewModel.checkIn() },
                        checkInCount = uiState.checkInCount,
                        isCheckInLoading = uiState.isCheckInLoading,
                        hasCheckedInToday = uiState.hasCheckedInToday
                    )
                }
            }
        }
    }

    // Login dialog for auth-protected features
    if (showLoginDialog) {
        ProfileDialog(
            authState = authState,
            onGoogleSignInResult = { result ->
                when (result) {
                    is GoogleSignInResult.Success -> {
                        authViewModel.signInWithGoogle(result.idToken, result.accessToken)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("登入成功")
                        }
                        // Refresh check-in status after login
                        viewModel.refreshCheckInStatus()
                    }
                    is GoogleSignInResult.Error -> {
                        // Reopen dialog to show error
                        showLoginDialog = true
                    }
                    is GoogleSignInResult.Cancelled -> {
                        // User cancelled
                    }
                }
            },
            onGoogleSignInStarted = {
                // Dismiss dialog when sign-in starts to avoid UI overlap
                showLoginDialog = false
            },
            onSignOut = { authViewModel.signOut() },
            onDismiss = {
                showLoginDialog = false
                authViewModel.clearError()
            }
        )
    }

    // Report dialog
    if (showReportDialog) {
        ReportDialog(
            onSubmit = { category, comment ->
                viewModel.submitReport(category.value, comment)
            },
            onDismiss = {
                showReportDialog = false
                viewModel.clearReportState()
            },
            isLoading = uiState.isReportLoading,
            error = uiState.reportError
        )
    }
}

@Composable
private fun LoadingContent() {
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
                text = "載入中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("重試")
            }
        }
    }
}

private fun ParkingSpot.hasValidCoordinates(): Boolean {
    return latitude != 0.0 || longitude != 0.0
}

@Composable
private fun SpotDetailContent(
    spot: ParkingSpot,
    onOpenMaps: () -> Unit,
    onReport: () -> Unit,
    onCheckIn: () -> Unit,
    checkInCount: Int,
    isCheckInLoading: Boolean,
    hasCheckedInToday: Boolean
) {
    val hasValidCoordinates = spot.hasValidCoordinates()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Mini Map
        MiniMapSection(spot = spot)

        // Spot Information
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name and Verified Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (spot.isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "已驗證",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Address
            InfoRow(
                icon = Icons.Default.Place,
                label = "地址",
                value = spot.address
            )

            // Check-in count
            if (checkInCount > 0) {
                InfoRow(
                    icon = Icons.Default.People,
                    label = "打卡人數",
                    value = "已有 $checkInCount 人停過"
                )
            }

            // Plate Types
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "車牌類型",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                spot.plateTypes.forEach { plateType ->
                    PlateTypeBadge(plateType = plateType)
                }
            }

            // Capacity
            spot.capacity?.let { capacity ->
                InfoRow(
                    icon = Icons.Default.LocalParking,
                    label = "容量",
                    value = "可停 $capacity 輛"
                )
            }

            // Source
            InfoRow(
                icon = Icons.Default.Source,
                label = "資料來源",
                value = spot.source.displayName()
            )

            // Description
            spot.description?.let { description ->
                InfoRow(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    label = "備註",
                    value = description
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.Map,
                    label = "開啟地圖",
                    onClick = onOpenMaps,
                    enabled = hasValidCoordinates
                )
                CheckInButton(
                    onClick = onCheckIn,
                    isLoading = isCheckInLoading,
                    hasCheckedIn = hasCheckedInToday
                )
                ActionButton(
                    icon = Icons.Default.Flag,
                    label = "回報問題",
                    onClick = onReport
                )
            }
        }
    }
}

@Composable
private fun MiniMapSection(spot: ParkingSpot) {
    MiniMap(
        spot = spot,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun PlateTypeBadge(plateType: PlateType) {
    val color = when (plateType) {
        PlateType.YELLOW -> Color(0xFFFFC107)
        PlateType.RED -> Color(0xFFD32F2F)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = plateType.displayName(),
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            enabled = enabled
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun CheckInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    hasCheckedIn: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            enabled = !isLoading && !hasCheckedIn
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else if (hasCheckedIn) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已打卡"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "打卡"
                )
            }
        }
        Text(
            text = if (hasCheckedIn) "已打卡" else "打卡",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
