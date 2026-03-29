package com.jp319.zerochan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DraculaDarkColorScheme = darkColorScheme(
    primary                = DraculaBurntOrange,
    onPrimary              = Color(0xFF1A0A40),
    primaryContainer       = DraculaCurrentLine,
    onPrimaryContainer     = Color(0xFFE0D0FF),

    secondary              = DraculaOrange,
    onSecondary            = Color(0xFF3D001F),
    secondaryContainer     = DraculaCurrentLine,
    onSecondaryContainer   = Color(0xFFFFD0F0),

    tertiary               = DraculaCyan,
    onTertiary             = Color(0xFF003642),
    tertiaryContainer      = DraculaCurrentLine,
    onTertiaryContainer    = Color(0xFFC8F6FF),

    error                  = DraculaRed,
    onError                = Color(0xFF2C0000),
    errorContainer         = Color(0xFF7A1919),
    onErrorContainer       = Color(0xFFFFB3B3),

    background             = DraculaBackground,
    onBackground           = DraculaForeground,
    surface                = DraculaBackground,
    onSurface              = DraculaForeground,
    surfaceVariant         = DraculaCurrentLine,
    onSurfaceVariant       = Color(0xFFBBBDC8),

    outline                = DraculaComment,
    outlineVariant         = DraculaCurrentLine,

    inverseSurface         = DraculaForeground,
    inverseOnSurface       = DraculaBackground,
    inversePrimary         = Color(0xFF6030C0),

    scrim                  = Color(0xFF000000),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DraculaDarkColorScheme,
        content = content,
    )
}