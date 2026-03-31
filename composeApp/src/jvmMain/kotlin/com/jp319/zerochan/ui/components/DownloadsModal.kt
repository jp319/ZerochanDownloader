package com.jp319.zerochan.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.jp319.zerochan.data.network.DownloadProgressTracker
import com.jp319.zerochan.ui.screens.DownloadJob
import com.jp319.zerochan.ui.screens.DownloadState
import com.jp319.zerochan.utils.FileUtil
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import java.io.File

/**
 * A unified dialog that combines the Downloads Library and the Active Download Manager.
 */
@Composable
fun DownloadsModal(
    currentPath: String,
    localFiles: List<File>,
    downloadQueue: List<DownloadJob>,
    onChangePath: (String) -> Unit,
    onDismiss: () -> Unit,
    onImageClick: (File) -> Unit,
    onClearCompleted: () -> Unit,
    onRetry: (DownloadJob) -> Unit,
    onRetryAll: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Local Library", "Active Tasks")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Downloads Center",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTab == 0) "${localFiles.size} images cached" else "${downloadQueue.size} active tasks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AppTooltip(text = "Close") {
                        IconButton(onClick = onDismiss) {
                            Icon(TablerIcons.X, contentDescription = "Close")
                        }
                    }
                }

                // TABS
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                BadgedBox(
                                    badge = {
                                        if (index == 1 && downloadQueue.any { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.PREPARING }) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                ) {
                                    Text(
                                        title,
                                        style = if (selectedTab == index) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        else MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    when (selectedTab) {
                        0 -> LibraryTabContent(currentPath, localFiles, onChangePath, onImageClick)
                        1 -> DownloadManagerTabContent(downloadQueue, onClearCompleted, onRetry, onRetryAll)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTabContent(
    currentPath: String,
    localFiles: List<File>,
    onChangePath: (String) -> Unit,
    onImageClick: (File) -> Unit,
) {
    val currentDir = remember(currentPath) { File(currentPath).apply { if (!exists()) mkdirs() } }
    val fileStore = remember(currentDir) { runCatching { java.nio.file.Files.getFileStore(currentDir.toPath()) }.getOrNull() }
    val totalSpace = fileStore?.totalSpace ?: 0L
    val freeSpace = fileStore?.usableSpace ?: 0L
    val folderSize = localFiles.sumOf { it.length() }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024f * 1024f))
            else -> String.format("%.2f GB", bytes / (1024f * 1024f * 1024f))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            onClick = { FileUtil.chooseDirectory(currentPath, onChangePath) },
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(TablerIcons.Folder, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Save Location", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(currentPath, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Directory Usage: ${formatSize(folderSize)} • Free System Space: ${formatSize(freeSpace)} / ${formatSize(totalSpace)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { FileUtil.openFileNatively(currentDir) }) {
                    Icon(TablerIcons.ExternalLink, "Open folder", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (localFiles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No images found in this directory.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(localFiles, key = { it.absolutePath }) { file ->
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray)
                            .clickable { onImageClick(file) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadManagerTabContent(
    queue: List<DownloadJob>,
    onClearCompleted: () -> Unit,
    onRetry: (DownloadJob) -> Unit,
    onRetryAll: () -> Unit,
) {
    val progressMap by DownloadProgressTracker.progress.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (queue.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hasStalled = queue.any { it.state == DownloadState.RETRY_STALLED || it.state == DownloadState.ERROR }
                if (hasStalled) {
                    TextButton(onClick = onRetryAll) {
                        Icon(TablerIcons.Refresh, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retry All")
                    }
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onClearCompleted) {
                    Icon(TablerIcons.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Clear Completed")
                }
            }
        }

        if (queue.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(TablerIcons.Download, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f))
                    Spacer(Modifier.height(16.dp))
                    Text("No active downloads", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(queue, key = { it.item.id }) { job ->
                    DownloadManagerRow(job, progressMap, onRetry)
                }
            }
        }
    }
}

@Composable
private fun DownloadManagerRow(job: DownloadJob, progressMap: Map<String, Float>, onRetry: (DownloadJob) -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = job.item.thumbnail.replace(".avif", ".jpg"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    job.item.tag.split(", ").firstOrNull() ?: "Untitled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                when (job.state) {
                    DownloadState.DOWNLOADING -> {
                        val progress = progressMap[job.resolvedUrl ?: ""] ?: 0f
                        val animatedProgress by animateFloatAsState(progress, spring(0.8f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    DownloadState.SUCCESS -> Text("Completed", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall)
                    DownloadState.ERROR -> Text(job.errorMessage ?: "Failed", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    DownloadState.PREPARING -> Text("Preparing...", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
                    DownloadState.RETRY_STALLED -> Text("Stalled", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.width(16.dp))

            if (job.state == DownloadState.ERROR || job.state == DownloadState.RETRY_STALLED) {
                IconButton(onClick = { onRetry(job) }) {
                    Icon(TablerIcons.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                }
            } else if (job.state == DownloadState.SUCCESS) {
                Icon(TablerIcons.Check, null, tint = Color(0xFF4CAF50))
            } else {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }
    }
}
