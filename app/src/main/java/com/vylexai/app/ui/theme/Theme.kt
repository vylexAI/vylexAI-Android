package com.vylexai.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = VylexPalette.Cyan400,
    onPrimary = VylexPalette.Ink900,
    primaryContainer = VylexPalette.Cyan500,
    onPrimaryContainer = VylexPalette.Text100,
    secondary = VylexPalette.Azure600,
    onSecondary = VylexPalette.Text100,
    tertiary = VylexPalette.Emerald400,
    onTertiary = VylexPalette.Ink900,
    background = VylexPalette.Ink900,
    onBackground = VylexPalette.Text100,
    surface = VylexPalette.Ink800,
    onSurface = VylexPalette.Text100,
    surfaceVariant = VylexPalette.Ink700,
    onSurfaceVariant = VylexPalette.Text300,
    surfaceContainer = VylexPalette.Ink700,
    surfaceContainerHigh = VylexPalette.Ink600,
    surfaceContainerHighest = VylexPalette.Ink500,
    outline = VylexPalette.Ink500,
    outlineVariant = VylexPalette.Ink600,
    error = VylexPalette.Rose400,
    onError = VylexPalette.Ink900
)

private val LightColors = lightColorScheme(
    primary = VylexPalette.Cyan500,
    onPrimary = VylexPalette.Text100,
    secondary = VylexPalette.Azure600,
    onSecondary = VylexPalette.Text100,
    background = VylexPalette.Text100,
    onBackground = VylexPalette.Ink900,
    surface = VylexPalette.Text100,
    onSurface = VylexPalette.Ink900
)

// Dark-first product decision: ignore the system setting until a light-mode
// pass is properly designed. Flip the `|| true` once that lands.
@Composable
fun VylexTheme(
    darkTheme: Boolean = isSystemInDarkTheme() || true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VylexTypography,
        content = content
    )
}
