package com.jp319.zerochan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.jp319.zerochan.utils.zoomable
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.animate

/**
 * A specialized viewer for animated GIF images that supports interactive
 * zooming and panning.
 *
 * @param animatedImage The loaded animated image object, or null if still loading.
 * @param progress The current download or parsing progress (0.0 to 1.0).
 * @param scale The current zoom scale factor.
 * @param offsetX The current horizontal offset for panning.
 * @param offsetY The current vertical offset for panning.
 * @param onTransform Callback triggered as the user zooms or pans.
 * @param onReset Callback triggered (e.g., via double tap) to reset transformations.
 * @param modifier Modifier to be applied to the root container.
 */
@Composable
fun ZoomableGifViewer(
    animatedImage: AnimatedImage?,
    progress: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onTransform: (zoom: Float, panX: Float, panY: Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (animatedImage == null) {
            LoadingProgress(progress = progress)
        } else {
            // The actual GIF with Zoom/Pan
            Image(
                bitmap = animatedImage.animate(),
                contentDescription = "Animated GIF",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .zoomable(
                            scale = scale,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            onTransform = onTransform,
                            onReset = onReset,
                        ),
            )
        }
    }
}
