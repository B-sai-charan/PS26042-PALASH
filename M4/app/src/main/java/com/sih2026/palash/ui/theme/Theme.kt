package com.sih2026.palash.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val M6LightColorScheme = lightColorScheme(
    primary = M6PrimaryAmber,
    onPrimary = Color.White,
    primaryContainer = M6SoftYellowSurface,
    onPrimaryContainer = M6DarkAmber,
    secondary = M6DarkAmber,
    onSecondary = Color.White,
    secondaryContainer = M6SoftYellowSurface,
    onSecondaryContainer = M6SecondaryText,
    tertiary = M6LightOrangeAccent,
    onTertiary = M6PrimaryText,
    background = M6Background,
    onBackground = M6PrimaryText,
    surface = M6SoftYellowSurface,
    onSurface = M6PrimaryText,
    surfaceVariant = M6SoftYellowSurface,
    onSurfaceVariant = M6SecondaryText,
    outline = M6LightOrangeAccent,
    error = M6Error,
    onError = Color.White,
    errorContainer = M6ErrorLight,
    onErrorContainer = M6Error
)

private val M6DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8A65),
    onPrimary = Color(0xFF3E1200),
    primaryContainer = Color(0xFF5D1D00),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF00390A),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFA5D6A7),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF4A2800),
    background = Color(0xFF161418),
    onBackground = Color(0xFFF0EAE1),
    surface = Color(0xFF242226),
    onSurface = Color(0xFFF0EAE1),
    surfaceVariant = Color(0xFF322E36),
    onSurfaceVariant = Color(0xFFD4CDD8),
    outline = Color(0xFF4C4554)
)

@Composable
fun PalashTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode.value,
    dynamicColor: Boolean = false, // Preserve custom brand identity on API 31+
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) M6DarkColorScheme else M6LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
