package com.jp319.zerochan.utils

import androidx.compose.ui.graphics.Color

object ColorUtils {
    /**
     * Parses a hex color string into a Compose [Color].
     * Supports both "#RRGGBB" and "AARRGGBB" formats.
     *
     * @param hex The hex string to parse.
     * @param fallback The color to return if parsing fails.
     * @return The parsed color or the fallback.
     */
    fun parseHexColor(
        hex: String,
        fallback: Color,
    ): Color {
        try {
            val clean = if (hex.startsWith("#")) hex.substring(1) else hex
            val argb = if (clean.length == 6) java.lang.Long.parseLong("FF$clean", 16) else java.lang.Long.parseLong(clean, 16)
            return Color(argb)
        } catch (_: Exception) {
            return fallback
        }
    }
}
