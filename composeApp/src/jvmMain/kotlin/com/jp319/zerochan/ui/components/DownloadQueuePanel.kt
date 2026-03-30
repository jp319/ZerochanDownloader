package com.jp319.zerochan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun DownloadQueuePanel(
    queue: List<DownloadJob>,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (queue.isEmpty()) return

    // Collect the global OkHttp progress map
    val progressMap by DownloadProgressTracker.progress.collectAsState()

    Card(
        modifier = modifier.width(350.dp).padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
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
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
            )

            // The List of Jobs
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
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            AsyncImage(
                model = job.item.thumbnail.replace(".avif", ".jpg"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(Color.DarkGray),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text & Progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.item.tag,
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
                    DownloadState.SUCCESS -> Text("Complete", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                    DownloadState.ERROR ->
                        Text(
                            "Failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    DownloadState.DOWNLOADING -> {
                        // Look up the live progress using the resolved URL!
                        val progress = job.resolvedUrl?.let { progressMap[it] } ?: 0f
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.weight(1f).height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Status Icons
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
