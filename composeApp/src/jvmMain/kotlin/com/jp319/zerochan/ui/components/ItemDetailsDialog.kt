package com.jp319.zerochan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.data.model.ZerochanFullItem
import compose.icons.TablerIcons
import compose.icons.tablericons.*

/**
 * A dialog displaying comprehensive metadata for a single Zerochan image.
 * Includes dimensions, file size, source URL, and an interactive tag cloud.
 *
 * @param details The detailed metadata for the image, or null if loading.
 * @param isLoading Whether the metadata fetch is currently in progress.
 * @param onDismiss Callback to close the dialog.
 * @param onDownload Callback to initiate a download of the full-resolution image.
 * @param onSearchTag Callback to close this dialog and search for a specific tag.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDetailsDialog(
    details: ZerochanFullItem?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onSearchTag: (String) -> Unit,
) {
    if (details == null && !isLoading) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(TablerIcons.InfoCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Image Details")
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (details != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        // Meta Info Grid
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailRow(TablerIcons.Maximize, "Resolution", "${details.width} x ${details.height}")

                                // Convert bytes to MB
                                val sizeInMb = details.size?.div((1024f * 1024f))
                                DetailRow(TablerIcons.Database, "File Size", "%.2f MB".format(sizeInMb))

                                if (!details.source.isNullOrBlank()) {
                                    DetailRow(TablerIcons.Link, "Source", details.source)
                                }
                            }
                        }
                    }

                    item {
                        Text("Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Tag Cloud
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            details.tags.forEach { tag ->
                                var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                                Box {
                                    AppTooltip(text = "Click to search or copy tag") {
                                        SuggestionChip(
                                            onClick = { expanded = true },
                                            label = { Text(tag) },
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Search Tag") },
                                            onClick = {
                                                expanded = false
                                                onSearchTag(tag)
                                                onDismiss()
                                            },
                                            leadingIcon = { Icon(TablerIcons.Search, null) },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Copy Tag") },
                                            onClick = {
                                                expanded = false
                                                val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                                                val selection = java.awt.datatransfer.StringSelection(tag)
                                                clipboard.setContents(selection, null)
                                            },
                                            leadingIcon = { Icon(TablerIcons.Copy, null) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Add the explicit URL Download Button
                if (details != null) {
                    AppTooltip(text = "Download original resolution image") {
                        Button(onClick = onDownload) {
                            Icon(TablerIcons.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download Original")
                        }
                    }
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
    )
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
