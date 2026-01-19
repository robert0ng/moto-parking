package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    onSpotClick: (spotId: String) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(Screen.LIST) }
    var selectedRadius by remember { mutableStateOf(1000) }
    var showRadiusMenu by remember { mutableStateOf(false) }

    val radiusOptions = listOf(500, 1000, 2000, 5000)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("大重機停車位 (${formatRadius(selectedRadius)})") },
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
            when (currentScreen) {
                Screen.MAP -> MapScreenPlaceholder()
                Screen.LIST -> ListScreen(
                    radiusMeters = selectedRadius,
                    onSpotClick = onSpotClick
                )
            }
        }
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
