package com.jp319.zerochan.utils

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

/**
 * A combined extension to handle zooming (pinch, scroll) and panning (drag).
 *
 * @param scale The current zoom scale factor (1.0 = 100%).
 * @param offsetX The current horizontal offset for panning.
 * @param offsetY The current vertical offset for panning.
 * @param onTransform Callback triggered as the user zooms or pans.
 * @param onReset Callback triggered (e.g., via double tap) to reset transformations.
 */
fun Modifier.zoomable(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onTransform: (zoom: Float, panX: Float, panY: Float) -> Unit,
    onReset: () -> Unit,
): Modifier =
    composed {
        val currentScale by rememberUpdatedState(scale)
        val currentOffsetX by rememberUpdatedState(offsetX)
        val currentOffsetY by rememberUpdatedState(offsetY)
        val currentOnTransform by rememberUpdatedState(onTransform)
        val currentOnReset by rememberUpdatedState(onReset)

        this
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (currentScale * zoom).coerceIn(1f, 10f)
                    if (newScale > 1f) {
                        currentOnTransform(newScale, currentOffsetX + pan.x, currentOffsetY + pan.y)
                    } else {
                        currentOnReset()
                    }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val deltaY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            val newScale = (currentScale - deltaY * 0.15f).coerceIn(1f, 10f)
                            if (newScale > 1f) {
                                currentOnTransform(newScale, currentOffsetX, currentOffsetY)
                            } else {
                                currentOnReset()
                            }
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    currentOnReset()
                })
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY,
            )
    }
