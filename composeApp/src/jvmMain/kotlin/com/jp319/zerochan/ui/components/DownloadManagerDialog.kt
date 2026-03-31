package com.jp319.zerochan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.X

/**
 * A dedicated modal dialog for managing the download queue.
 * Displays progress, status, and allows for retries or clearing the list.
 *
 * @param show Whether the dialog is currently visible.
 * @param queue The list of all download jobs (active, pending, finished).
 * @param onDismiss Callback to close the dialog.
 * @param onClearCompleted Callback to remove finished jobs from the list.
 * @param onRetry Callback to re-attempt a failed or stalled download.
 * @param onRetryAll Callback to re-attempt all retriable jobs at once.
 */
@Composable
fun DownloadManagerDialog(
    show: Boolean,
    queue: List<DownloadJob>,
    onDismiss: () -> Unit,
    onClearCompleted: () -> Unit,
    onRetry: (DownloadJob) -> Unit,
    onRetryAll: () -> Unit,
) {
    if (!show) return

    val progressMap by DownloadProgressTracker.progress.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Download Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (queue.isNotEmpty()) {
                            Text(
                                "${queue.count { it.state == DownloadState.SUCCESS }} of ${queue.size} completed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(TablerIcons.X, "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Actions Bar
                if (queue.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val hasStalled = queue.any { it.state == DownloadState.RETRY_STALLED || it.state == DownloadState.ERROR }
                        if (hasStalled) {
                            Button(
                                onClick = onRetryAll,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.height(36.dp),
                            ) {
                                Icon(TablerIcons.Refresh, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Retry All", style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(Modifier.width(12.dp))
                        }

                        OutlinedButton(
                            onClick = onClearCompleted,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Icon(TablerIcons.Check, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Clear Completed", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Content
                if (queue.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                TablerIcons.Check,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No active downloads",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(queue, key = { it.item.id }) { job ->
                            DownloadManagerRow(job, progressMap, onRetry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadManagerRow(
    job: DownloadJob,
    progressMap: Map<String, Float>,
    onRetry: (DownloadJob) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = job.item.thumbnail.replace(".avif", ".jpg"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.item.tag.split(", ").firstOrNull() ?: "Untitled Image",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                when (job.state) {
                    DownloadState.PREPARING -> {
                        Text(
                            "Waiting for sequential slot...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    DownloadState.DOWNLOADING -> {
                        val url = job.resolvedUrl
                        val progress = if (url != null) progressMap[url] ?: 0f else 0f
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f),
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    DownloadState.SUCCESS -> {
                        Text(
                            "Downloaded successfully",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    DownloadState.RETRY_STALLED -> {
                        Text(
                            "Stalled: High-res not found",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    DownloadState.ERROR -> {
                        Text(
                            job.errorMessage ?: "Download failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Action/Status Icon
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                when (job.state) {
                    DownloadState.SUCCESS -> Icon(TablerIcons.Check, null, tint = Color(0xFF4CAF50))
                    DownloadState.RETRY_STALLED, DownloadState.ERROR -> {
                        IconButton(onClick = { onRetry(job) }) {
                            Icon(TablerIcons.Refresh, "Retry", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    DownloadState.PREPARING, DownloadState.DOWNLOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
    }
}
