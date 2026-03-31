package com.jp319.zerochan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import com.jp319.zerochan.data.model.ZerochanItem
import com.jp319.zerochan.data.network.DownloadProgressTracker
import com.jp319.zerochan.utils.zoomable
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import org.jetbrains.compose.animatedimage.AnimatedImage
import java.io.File

/**
 * A full-screen dialog for previewing a selected Zerochan image in high resolution.
 * Supports zooming, panning, and basic actions like downloading or viewing details.
 * Also handles animated GIF playback.
 *
 * @param item The image item to display.
 * @param verifiedUrl The resolved high-resolution URL for the image.
 * @param onDismiss Callback to close the modal.
 * @param onViewDetails Callback to fetch and show detailed metadata for the item.
 * @param onDownload Callback to initiate a download of the full image.
 * @param fetchGifFile Suspend function to retrieve a local file for GIF playback.
 */
@Composable
fun ImageModal(
    item: ZerochanItem?,
    verifiedUrl: String?,
    onDismiss: () -> Unit,
    onViewDetails: (Int) -> Unit,
    onDownload: () -> Unit,
    fetchGifFile: suspend (Int, String) -> File?,
) {
    if (item == null) return

    // State for Zooming and Panning
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Reset zoom and pan when opening a new image
    LaunchedEffect(item.id) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        // prevent click-through
                        .clickable(enabled = false) { },
            ) {
                // Header with actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppTooltip(text = "Reset Zoom (100%)") {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                            enabled = scale != 1f || offsetX != 0f || offsetY != 0f,
                        ) {
                            Icon(TablerIcons.Maximize, contentDescription = "Reset Zoom")
                        }
                    }

                    AppTooltip(text = "Download original resolution image") {
                        FilledTonalButton(onClick = onDownload, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(TablerIcons.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download")
                        }
                    }

                    AppTooltip(text = "View tags, dimensions, and uploader") {
                        FilledTonalButton(
                            onClick = { onViewDetails(item.id) },
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Icon(TablerIcons.InfoCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View Details")
                        }
                    }

                    AppTooltip(text = "Close Preview") {
                        IconButton(
                            onClick = onDismiss,
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                        ) {
                            Icon(TablerIcons.CircleX, contentDescription = "Close")
                        }
                    }
                }

                // Add a state to track retries
                var retryTrigger by remember { mutableIntStateOf(0) }
                // Collect the global progress state
                val progressMap by DownloadProgressTracker.progress.collectAsState()

                // Extract the progress specifically for the URL we are trying to load
                val currentProgress = verifiedUrl?.let { progressMap[it] } ?: 0f

                // Image display container
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    // Catch null URL first to prevent Coil/GIF from panicking
                    if (verifiedUrl == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val isGif = verifiedUrl.endsWith(".gif", ignoreCase = true)

                        if (isGif) {
                            var animatedImage by remember(verifiedUrl) { mutableStateOf<AnimatedImage?>(null) }

                            LaunchedEffect(verifiedUrl) {
                                val tempFile = fetchGifFile(item.id, verifiedUrl)
                                if (tempFile != null) {
                                    animatedImage = org.jetbrains.compose.animatedimage.loadAnimatedImage(tempFile.absolutePath)
                                }
                            }

                            ZoomableGifViewer(
                                animatedImage = animatedImage,
                                progress = currentProgress,
                                scale = scale,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                onTransform = { s, x, y ->
                                    scale = s
                                    offsetX = x
                                    offsetY = y
                                },
                                onReset = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                            )
                        } else {
                            // Unified Progress UI for Coil Loading
                            key(retryTrigger) {
                                coil3.compose.SubcomposeAsyncImage(
                                    model =
                                        ImageRequest.Builder(LocalPlatformContext.current)
                                            .data(verifiedUrl)
                                            .size(Size.ORIGINAL)
                                            .build(),
                                    contentDescription = item.tag,
                                    contentScale = ContentScale.Fit,
                                    filterQuality = FilterQuality.High,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .zoomable(
                                                scale = scale,
                                                offsetX = offsetX,
                                                offsetY = offsetY,
                                                onTransform = { s, x, y ->
                                                    scale = s
                                                    offsetX = x
                                                    offsetY = y
                                                },
                                                onReset = {
                                                    scale = 1f
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                },
                                            ),
                                    loading = {
                                        LoadingProgress(progress = currentProgress)
                                    },
                                    error = {
                                        Column(
                                            modifier = Modifier.fillMaxSize().clickable { retryTrigger++ },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(
                                                TablerIcons.AlertTriangle,
                                                null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(48.dp),
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "Tap to retry",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.labelLarge,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                // Tag/Name footer
                Text(
                    text = item.tag,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
