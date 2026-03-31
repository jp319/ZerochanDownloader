package com.jp319.zerochan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun HslColorPicker(
    initialColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hsv by remember { 
        val hsvArr = FloatArray(3)
        java.awt.Color.RGBtoHSB(
            (initialColor.red * 255).toInt(),
            (initialColor.green * 255).toInt(),
            (initialColor.blue * 255).toInt(),
            hsvArr
        )
        mutableStateOf(Triple(hsvArr[0] * 360f, hsvArr[1], hsvArr[2]))
    }

    val hue = hsv.first
    val saturation = hsv.second
    val value = hsv.third

    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.aspectRatio(1f).fillMaxWidth()) {
            val center = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
            val outerRadius = constraints.maxWidth / 2f
            val innerRadius = outerRadius * 0.75f
            val ringWidth = outerRadius - innerRadius
            
            val squareSize = (innerRadius * sqrt(2f) * 0.9f)
            val squareTopLeft = Offset(center.x - squareSize / 2, center.y - squareSize / 2)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val pos = change.position
                            val dist = (pos - center).getDistance()
                            
                            if (dist in innerRadius..outerRadius) {
                                // Hue ring
                                val angle = (atan2(pos.y - center.y, pos.x - center.x) * 180 / PI).toFloat()
                                val normalizedAngle = (angle + 360) % 360
                                hsv = Triple(normalizedAngle, hsv.second, hsv.third)
                                onColorChanged(hsvToColor(normalizedAngle, hsv.second, hsv.third))
                            } else if (pos.x in squareTopLeft.x..(squareTopLeft.x + squareSize) &&
                                pos.y in squareTopLeft.y..(squareTopLeft.y + squareSize)) {
                                // SV square
                                val s = ((pos.x - squareTopLeft.x) / squareSize).coerceIn(0f, 1f)
                                val v = (1f - (pos.y - squareTopLeft.y) / squareSize).coerceIn(0f, 1f)
                                hsv = Triple(hsv.first, s, v)
                                onColorChanged(hsvToColor(hsv.first, s, v))
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            val dist = (pos - center).getDistance()
                            if (dist in (innerRadius - 10f)..(outerRadius + 10f)) {
                                val angle = (atan2(pos.y - center.y, pos.x - center.x) * 180 / PI).toFloat()
                                val normalizedAngle = (angle + 360) % 360
                                hsv = Triple(normalizedAngle, hsv.second, hsv.third)
                                onColorChanged(hsvToColor(normalizedAngle, hsv.second, hsv.third))
                            } else if (pos.x in squareTopLeft.x..(squareTopLeft.x + squareSize) &&
                                pos.y in squareTopLeft.y..(squareTopLeft.y + squareSize)) {
                                val s = ((pos.x - squareTopLeft.x) / squareSize).coerceIn(0f, 1f)
                                val v = (1f - (pos.y - squareTopLeft.y) / squareSize).coerceIn(0f, 1f)
                                hsv = Triple(hsv.first, s, v)
                                onColorChanged(hsvToColor(hsv.first, s, v))
                            }
                        }
                    }
            ) {
                // Hue Ring
                val sweepGradient = Brush.sweepGradient(
                    colors = List(360) { degree ->
                        Color.hsv(degree.toFloat(), 1f, 1f)
                    },
                    center = center
                )
                drawCircle(
                    brush = sweepGradient,
                    radius = outerRadius - ringWidth / 2f,
                    style = Stroke(width = ringWidth)
                )

                // Hue Indicator
                val angleRad = (hue * PI / 180f).toFloat()
                val indicatorPos = Offset(
                    center.x + (innerRadius + ringWidth / 2f) * cos(angleRad),
                    center.y + (innerRadius + ringWidth / 2f) * sin(angleRad)
                )
                drawCircle(Color.White, radius = 6.dp.toPx(), center = indicatorPos, style = Stroke(2.dp.toPx()))
                drawCircle(Color.Black, radius = 7.dp.toPx(), center = indicatorPos, style = Stroke(1.dp.toPx()))

                // SV Square
                // Saturation Gradient (Left to Right)
                val saturationBrush = Brush.linearGradient(
                    colors = listOf(Color.White, Color.hsv(hue, 1f, 1f)),
                    start = squareTopLeft,
                    end = Offset(squareTopLeft.x + squareSize, squareTopLeft.y)
                )
                drawRect(brush = saturationBrush, topLeft = squareTopLeft, size = Size(squareSize, squareSize))
                
                // Value Gradient (Bottom to Top Overlay)
                val valueBrush = Brush.linearGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    start = Offset(squareTopLeft.x, squareTopLeft.y + squareSize),
                    end = squareTopLeft
                )
                drawRect(brush = valueBrush, topLeft = squareTopLeft, size = Size(squareSize, squareSize))
                
                // SV Indicator
                val svIndicatorPos = Offset(
                    squareTopLeft.x + saturation * squareSize,
                    squareTopLeft.y + (1f - value) * squareSize
                )
                drawCircle(Color.White, radius = 5.dp.toPx(), center = svIndicatorPos, style = Stroke(2.dp.toPx()))
                drawCircle(Color.Black, radius = 6.dp.toPx(), center = svIndicatorPos, style = Stroke(1.dp.toPx()))
            }
        }
    }
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val rgb = java.awt.Color.HSBtoRGB(h / 360f, s, v)
    return Color(rgb)
}
