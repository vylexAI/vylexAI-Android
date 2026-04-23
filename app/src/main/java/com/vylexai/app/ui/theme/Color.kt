package com.vylexai.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand — derived from vylexai.com identity: teal-blue tree-of-intelligence mark on deep night canvas.
// Dark-first; a light theme exists but the product lives in dark.

object VylexPalette {
    // Core
    val Ink900 = Color(0xFF05070C) // app background, true night
    val Ink800 = Color(0xFF0A0F18) // surface
    val Ink700 = Color(0xFF111826) // elevated surface 1
    val Ink600 = Color(0xFF1A2334) // elevated surface 2
    val Ink500 = Color(0xFF283249) // dividers, outlines

    // Accent (brand teal-cyan → azure)
    val Cyan200 = Color(0xFF9EF2FF)
    val Cyan300 = Color(0xFF4FE3FF) // highlight
    val Cyan400 = Color(0xFF1AC8FF) // primary
    val Cyan500 = Color(0xFF0AA6F0) // primary pressed
    val Azure600 = Color(0xFF1E6EE8) // secondary, gradient end

    // Signal
    val Emerald400 = Color(0xFF2DE0A3) // earning / success
    val Amber400 = Color(0xFFFFB84D) // warning, thermal
    val Rose400 = Color(0xFFFF6B8A) // error, throttle

    // Text
    val Text100 = Color(0xFFF3F6FB) // primary text
    val Text300 = Color(0xFFB8C3D9) // secondary text
    val Text500 = Color(0xFF7B8AA5) // tertiary / captions

    // Gradients
    val BrandGradient = Brush.linearGradient(
        0.0f to Cyan300,
        0.6f to Cyan400,
        1.0f to Azure600
    )

    val SurfaceGlow = Brush.radialGradient(
        0.0f to Cyan400.copy(alpha = 0.18f),
        0.6f to Cyan400.copy(alpha = 0.04f),
        1.0f to Color.Transparent
    )
}
