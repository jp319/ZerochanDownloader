package com.jp319.zerochan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.animate

@Composable
fun ZoomableGifViewer(
    animatedImage: AnimatedImage?,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (animatedImage == null) {
            // Identical Loading UI to your Coil setup
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (progress > 0f && progress < 1f) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                    )
                } else {
                    // Spinner for finding URL or initial parsing
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            // The actual GIF with Zoom/Pan
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }

            Image(
                bitmap = animatedImage.animate(),
                contentDescription = "Animated GIF",
                contentScale = ContentScale.Fit,
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
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY),
            )
        }
    }
}
