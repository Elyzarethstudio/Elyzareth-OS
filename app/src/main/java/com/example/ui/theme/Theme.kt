package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElyzarethDarkColorScheme = darkColorScheme(
    primary = ElyCyan,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBAEAFF),
    secondary = ElyViolet,
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFEADDFF),
    tertiary = ElyG3Axiom,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005234),
    onTertiaryContainer = Color(0xFF6CF8B8),
    background = ElyBackground,
    onBackground = ElyTextPrimary,
    surface = ElySurfaceDark,
    onSurface = ElyTextPrimary,
    surfaceVariant = ElySurfaceCard,
    onSurfaceVariant = ElyTextSecondary,
    outline = ElyTaskbarBorder,
    error = ElyError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ElyzarethDarkColorScheme,
        typography = Typography,
        content = content
    )
}
