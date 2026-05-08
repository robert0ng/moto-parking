package com.motoparking.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motoparking.app.ui.viewmodels.PolicyGroup
import com.motoparking.app.ui.viewmodels.PolicyViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PolicyScreen(
    viewModel: PolicyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
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
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("重試")
                        }
                    }
                }
            }
            else -> {
                PolicyList(
                    alreadyOpen = uiState.alreadyOpen,
                    nowOpen = uiState.nowOpen,
                    upcoming = uiState.upcoming
                )
            }
        }
    }
}

@Composable
private fun PolicyList(
    alreadyOpen: List<PolicyGroup>,
    nowOpen: List<PolicyGroup>,
    upcoming: List<PolicyGroup>
) {
    val isEmpty = alreadyOpen.isEmpty() && nowOpen.isEmpty() && upcoming.isEmpty()
    if (isEmpty) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "目前尚無政策資料",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (nowOpen.isNotEmpty()) {
            item { SectionHeader("🟢 近期開放") }
            items(nowOpen) { group -> PolicyCard(group, isUpcoming = false) }
        }
        if (upcoming.isNotEmpty()) {
            if (nowOpen.isNotEmpty()) item { Spacer8() }
            item { SectionHeader("🟡 即將開放") }
            items(upcoming) { group -> PolicyCard(group, isUpcoming = true) }
        }
        if (alreadyOpen.isNotEmpty()) {
            if (nowOpen.isNotEmpty() || upcoming.isNotEmpty()) item { Spacer8() }
            item { SectionHeader("✅ 已開放") }
            items(alreadyOpen) { group -> PolicyCard(group, isUpcoming = false) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun Spacer8() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
}

@Composable
private fun PolicyCard(group: PolicyGroup, isUpcoming: Boolean) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${group.city} ${group.districts.joinToString(" ")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            val whenText = if (isUpcoming) {
                "自 ${group.effectiveDate} 起 (再 ${group.daysUntil} 天)"
            } else {
                "自 ${group.effectiveDate} 起"
            }
            Text(
                text = whenText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            group.feeDescription?.takeIf { it.isNotBlank() }?.let { fee ->
                Text(
                    text = fee,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            group.sourceUrl?.takeIf { it.isNotBlank() }?.let { url ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                TextButton(
                    onClick = { uriHandler.openUri(url) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = group.sourceLabel?.takeIf { it.isNotBlank() } ?: "查看公告",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
