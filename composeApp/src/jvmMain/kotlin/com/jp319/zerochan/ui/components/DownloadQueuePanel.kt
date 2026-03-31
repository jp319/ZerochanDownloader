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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jp319.zerochan.data.network.DownloadProgressTracker
import com.jp319.zerochan.ui.screens.DownloadJob
import com.jp319.zerochan.ui.screens.DownloadState
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.Check

/**
 * A floating UI panel that displays the current download queue.
 * Each job shows its progress, filename, and final status (Success, Error).
 *
 * @param queue The list of download jobs in the queue.
 * @param onClearCompleted Callback to remove finished (Success/Error) jobs from the queue.
 * @param modifier Modifier to be applied to the panel card.
 */
@Composable
fun DownloadQueuePanel(
    queue: List<DownloadJob>,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (queue.isEmpty()) return

    val progressMap by DownloadProgressTracker.progress.collectAsState()

    Card(
        modifier = modifier.width(350.dp).padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Downloads", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClearCompleted) {
                    Text("Clear Done")
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(queue, key = { it.item.id }) { job ->
                    DownloadRow(job, progressMap)
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    job: DownloadJob,
    progressMap: Map<String, Float>,
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = job.item.thumbnail.replace(".avif", ".jpg"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(Color.DarkGray),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.item.tag.split(", ").firstOrNull() ?: "Untitled",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    when (job.state) {
                        DownloadState.PREPARING ->
                            Text(
                                "Finding high-res...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        DownloadState.SUCCESS ->
                            Text("Complete", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                        DownloadState.ERROR ->
                            Text(
                                "Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        DownloadState.DOWNLOADING -> {
                            val url = job.resolvedUrl
                            val targetProgress = if (url != null) progressMap[url] ?: 0f else 0f
                            val animatedProgress by animateFloatAsState(
                                targetValue = targetProgress,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${(targetProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                when (job.state) {
                    DownloadState.SUCCESS ->
                        Icon(
                            TablerIcons.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp),
                        )
                    DownloadState.ERROR ->
                        Icon(
                            TablerIcons.AlertTriangle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    DownloadState.PREPARING, DownloadState.DOWNLOADING ->
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                }
            }
        }
    }
}
