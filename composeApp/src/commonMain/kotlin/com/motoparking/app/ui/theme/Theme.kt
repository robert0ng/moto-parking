package com.motoparking.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD32F2F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFF9A825),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF9C4),
    onSecondaryContainer = Color(0xFF1B1400),
    tertiary = Color(0xFF1976D2),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF001F5B),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFF370B1E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFEF9A9A),
    onPrimary = Color(0xFF5F1412),
    primaryContainer = Color(0xFF7E2A27),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFF176),
    onSecondary = Color(0xFF1B1400),
    secondaryContainer = Color(0xFF4A3C00),
    onSecondaryContainer = Color(0xFFFFF9C4),
    tertiary = Color(0xFF90CAF9),
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF00497D),
    onTertiaryContainer = Color(0xFFD1E4FF),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
