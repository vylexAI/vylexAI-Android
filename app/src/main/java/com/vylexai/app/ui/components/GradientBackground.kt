package com.vylexai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.material3.MaterialTheme
import com.vylexai.app.ui.theme.VylexPalette

/** Night canvas with a subtle cyan aurora — product-wide backdrop. */
@Composable
fun VylexBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surface = MaterialTheme.colorScheme.background
    val aurora = Brush.radialGradient(
        0.0f to VylexPalette.Cyan400.copy(alpha = 0.10f).compositeOver(surface),
        0.45f to VylexPalette.Azure600.copy(alpha = 0.05f).compositeOver(surface),
        1.0f to surface,
        radius = 1400f,
        tileMode = TileMode.Clamp
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
            .background(aurora),
        content = content
    )
}
