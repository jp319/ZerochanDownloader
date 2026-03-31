package com.jp319.zerochan.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.hct.Hct
import com.materialkolor.palettes.TonalPalette

private fun parseHexColor(hex: String, fallback: Color): Color {
    try {
        val clean = if (hex.startsWith("#")) hex.substring(1) else hex
        val argb = if (clean.length == 6) java.lang.Long.parseLong("FF$clean", 16) else java.lang.Long.parseLong(clean, 16)
        return Color(argb)
    } catch (e: Exception) {
        return fallback
    }
}

@Composable
fun AppTheme(
    themePreference: String = "#FFB86C", // Default hex (Orange)
    themeMode: String = "Dark",
    content: @Composable () -> Unit,
) {
    // Migrate legacy named themes
    val seedHex = LegacyThemeMap[themePreference] ?: themePreference
    val seedColor = parseHexColor(seedHex, DraculaBurntOrange)
    val seedInt = seedColor.toArgb()
    val isDark = themeMode == "Dark" || themeMode == "AMOLED"
    val isAmoled = themeMode == "AMOLED"

    val hct = Hct.fromInt(seedInt)
    val primaryPalette = TonalPalette.fromInt(seedInt)
    val secondaryPalette = TonalPalette.fromHueAndChroma(hct.hue, 16.0)
    val tertiaryPalette = TonalPalette.fromHueAndChroma(hct.hue + 60.0, 24.0)
    val neutralPalette = TonalPalette.fromHueAndChroma(hct.hue, 4.0)
    val neutralVariantPalette = TonalPalette.fromHueAndChroma(hct.hue, 8.0)
    val errorPalette = TonalPalette.fromHueAndChroma(25.0, 84.0)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = Color(primaryPalette.tone(80)),
            onPrimary = Color(primaryPalette.tone(20)),
            primaryContainer = Color(primaryPalette.tone(30)),
            onPrimaryContainer = Color(primaryPalette.tone(90)),
            secondary = Color(secondaryPalette.tone(80)),
            onSecondary = Color(secondaryPalette.tone(20)),
            secondaryContainer = Color(secondaryPalette.tone(30)),
            onSecondaryContainer = Color(secondaryPalette.tone(90)),
            tertiary = Color(tertiaryPalette.tone(80)),
            onTertiary = Color(tertiaryPalette.tone(20)),
            tertiaryContainer = Color(tertiaryPalette.tone(30)),
            onTertiaryContainer = Color(tertiaryPalette.tone(90)),
            error = Color(errorPalette.tone(80)),
            onError = Color(errorPalette.tone(20)),
            errorContainer = Color(errorPalette.tone(30)),
            onErrorContainer = Color(errorPalette.tone(90)),
            background = if (isAmoled) Color.Black else Color(neutralPalette.tone(10)),
            onBackground = Color(neutralPalette.tone(90)),
            surface = if (isAmoled) Color.Black else Color(neutralPalette.tone(10)),
            onSurface = Color(neutralPalette.tone(90)),
            surfaceVariant = Color(neutralVariantPalette.tone(30)),
            onSurfaceVariant = Color(neutralVariantPalette.tone(80)),
            outline = Color(neutralVariantPalette.tone(60)),
            outlineVariant = Color(neutralVariantPalette.tone(30)),
            scrim = Color(0xFF000000),
            inverseSurface = Color(neutralPalette.tone(90)),
            inverseOnSurface = Color(neutralPalette.tone(20)),
            inversePrimary = Color(primaryPalette.tone(40)),
        )
    } else {
        lightColorScheme(
            primary = Color(primaryPalette.tone(40)),
            onPrimary = Color(primaryPalette.tone(100)),
            primaryContainer = Color(primaryPalette.tone(90)),
            onPrimaryContainer = Color(primaryPalette.tone(10)),
            secondary = Color(secondaryPalette.tone(40)),
            onSecondary = Color(secondaryPalette.tone(100)),
            secondaryContainer = Color(secondaryPalette.tone(90)),
            onSecondaryContainer = Color(secondaryPalette.tone(10)),
            tertiary = Color(tertiaryPalette.tone(40)),
            onTertiary = Color(tertiaryPalette.tone(100)),
            tertiaryContainer = Color(tertiaryPalette.tone(90)),
            onTertiaryContainer = Color(tertiaryPalette.tone(10)),
            error = Color(errorPalette.tone(40)),
            onError = Color(errorPalette.tone(100)),
            errorContainer = Color(errorPalette.tone(90)),
            onErrorContainer = Color(errorPalette.tone(10)),
            background = Color(neutralPalette.tone(99)),
            onBackground = Color(neutralPalette.tone(10)),
            surface = Color(neutralPalette.tone(99)),
            onSurface = Color(neutralPalette.tone(10)),
            surfaceVariant = Color(neutralVariantPalette.tone(90)),
            onSurfaceVariant = Color(neutralVariantPalette.tone(30)),
            outline = Color(neutralVariantPalette.tone(50)),
            outlineVariant = Color(neutralVariantPalette.tone(80)),
            scrim = Color(0xFF000000),
            inverseSurface = Color(neutralPalette.tone(20)),
            inverseOnSurface = Color(neutralPalette.tone(95)),
            inversePrimary = Color(primaryPalette.tone(80)),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
