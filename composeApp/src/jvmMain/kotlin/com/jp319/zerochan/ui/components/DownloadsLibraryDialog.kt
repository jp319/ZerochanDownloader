package com.jp319.zerochan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.jp319.zerochan.utils.FileUtil
import compose.icons.TablerIcons
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.Folder
import compose.icons.tablericons.X
import java.io.File

@Composable
fun DownloadsLibraryDialog(
    currentPath: String,
    localFiles: List<File>,
    onChangePath: (String) -> Unit,
    onDismiss: () -> Unit,
    onImageClick: (File) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            val currentDir = remember(currentPath) { File(currentPath) }
            val totalSpace = remember(currentDir) { currentDir.totalSpace }
            val freeSpace = remember(currentDir) { currentDir.usableSpace }
            val folderSize = remember(localFiles) { localFiles.sumOf { it.length() } }

            fun formatSize(bytes: Long): String {
                return if (bytes < 1024) "$bytes B"
                else if (bytes < 1024 * 1024) "${bytes / 1024} KB"
                else if (bytes < 1024 * 1024 * 1024) String.format("%.1f MB", bytes / (1024f * 1024f))
                else String.format("%.2f GB", bytes / (1024f * 1024f * 1024f))
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // Header & Controls
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Downloads Library",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    AppTooltip(text = "Close Library") {
                        IconButton(onClick = onDismiss) {
                            Icon(TablerIcons.X, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Directory Path Selector
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { FileUtil.chooseDirectory(currentPath, onChangePath) },
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(TablerIcons.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Save Location", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(currentPath, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = "Directory Usage: ${formatSize(folderSize)} • Free System Space: ${formatSize(freeSpace)} / ${formatSize(totalSpace)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AppTooltip(text = "Open folder in File Explorer") {
                            IconButton(onClick = { FileUtil.openFileNatively(currentDir) }) {
                                Icon(TablerIcons.ExternalLink, contentDescription = "Open Directory in Explorer", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        AppTooltip(text = "Change download directory") {
                            Button(onClick = { FileUtil.chooseDirectory(currentPath, onChangePath) }) {
                                Text("Change")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // The Local Files Grid
                if (localFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No images found in this directory.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(localFiles, key = { it.absolutePath }) { file ->
                            AsyncImage(
                                model = file,
                                contentDescription = file.name,
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.DarkGray)
                                        .clickable { onImageClick(file) },
                            )
                        }
                    }
                }
            }
        }
    }
}
