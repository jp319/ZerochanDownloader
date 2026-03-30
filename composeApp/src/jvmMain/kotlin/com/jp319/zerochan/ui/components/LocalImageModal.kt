package com.jp319.zerochan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import compose.icons.TablerIcons
import compose.icons.tablericons.CircleX
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.loadAnimatedImage
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocalImageModal(
    file: File?,
    onDismiss: () -> Unit,
) {
    if (file == null) return

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Reset zoom when the file changes
    LaunchedEffect(file.absolutePath) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(24.dp).clickable(enabled = false) {},
            ) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = onDismiss,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = Color.White,
                            ),
                    ) { Icon(TablerIcons.CircleX, contentDescription = "Close") }
                }

                // Image Container
                Surface(shape = RoundedCornerShape(8.dp), color = Color.Transparent, modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val isGif = file.extension.equals("gif", ignoreCase = true)

                    if (isGif) {
                        var animatedImage by remember(file.absolutePath) { mutableStateOf<AnimatedImage?>(null) }

                        LaunchedEffect(file.absolutePath) {
                            // Pass the actual path string
                            animatedImage = loadAnimatedImage(file.absolutePath)
                        }

                        if (animatedImage == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            ZoomableGifViewer(
                                animatedImage!!,
                                progress = 1f, // Since it's loaded from disk, we can assume it's fully loaded
                            )
                        }
                    } else {
                        SubcomposeAsyncImage(
                            // Force Coil to read the ORIGNAL massive file size from disk
                            model =
                                ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(file)
                                    .size(Size.ORIGINAL)
                                    .build(),
                            contentDescription = file.name,
                            contentScale = ContentScale.Fit,
                            filterQuality = FilterQuality.High, // High-quality GPU filtering
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1f, 5f)
                                            if (scale > 1f) {
                                                offsetX += pan.x
                                                offsetY += pan.y
                                            } else {
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                if (event.type == PointerEventType.Scroll) {
                                                    val deltaY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                                    scale = (scale - deltaY * 0.15f).coerceIn(1f, 5f)
                                                    if (scale <= 1f) {
                                                        offsetX = 0f
                                                        offsetY = 0f
                                                    }
                                                    event.changes.forEach { it.consume() }
                                                }
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(onDoubleTap = {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        })
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY,
                                    ),
                        )
                    }
                }

                // Footer (File Name)
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
