package com.motoparking.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

enum class ReportCategory(val displayName: String, val value: String) {
    WRONG_LOCATION("位置不正確", "wrong_location"),
    CLOSED("已關閉/不存在", "closed"),
    WRONG_CAPACITY("容量錯誤", "wrong_capacity"),
    WRONG_PLATES("車牌類型錯誤", "wrong_plates"),
    OTHER("其他", "other")
}

@Composable
fun ReportDialog(
    onSubmit: (category: ReportCategory, comment: String?) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var selectedCategory by remember { mutableStateOf<ReportCategory?>(null) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "回報問題",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "關閉")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "請選擇問題類型",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category selection
                Column(modifier = Modifier.selectableGroup()) {
                    ReportCategory.entries.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Comment field (optional)
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= 100) comment = it },
                    label = { Text("補充說明（選填）") },
                    placeholder = { Text("最多100字") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = {
                        Text("${comment.length}/100")
                    }
                )

                // Error message
                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Loading indicator
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let { category ->
                        onSubmit(category, comment.ifBlank { null })
                    }
                },
                enabled = selectedCategory != null && !isLoading
            ) {
                Text("送出")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("取消")
            }
        }
    )
}
