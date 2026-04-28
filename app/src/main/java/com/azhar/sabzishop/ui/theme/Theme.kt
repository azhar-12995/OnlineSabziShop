package com.azhar.sabzishop.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SabziGreen,
    onPrimary = Color.White,
    primaryContainer = SabziGreenLight,
    onPrimaryContainer = Color.White,
    secondary = SabziOrange,
    onSecondary = Color.White,
    secondaryContainer = SabziOrangeLight,
    onSecondaryContainer = Color.White,
    tertiary = SabziTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    background = SabziBg,
    surface = SabziSurface,
    surfaceVariant = Color(0xFFE8F5E9),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF4E4E4E),
    error = SabziRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = SabziGreenLight,
    secondary = SabziOrangeLight,
    tertiary = SabziTeal,
)

@Composable
fun SabziShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}