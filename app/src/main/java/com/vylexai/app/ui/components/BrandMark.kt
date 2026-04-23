package com.vylexai.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.theme.VylexPalette

/**
 * Abstract "tree-of-intelligence" mark — procedurally drawn until the SVG asset lands.
 * A trunk with three branches, each ending in a node dot. Echoes the vylexai.com wordmark.
 */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 96.dp, tint: Color = VylexPalette.Cyan300) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = w * 0.08f)

            val trunk = Path().apply {
                moveTo(w * 0.5f, h * 0.85f)
                lineTo(w * 0.5f, h * 0.45f)
            }
            val left = Path().apply {
                moveTo(w * 0.5f, h * 0.55f)
                quadraticTo(w * 0.25f, h * 0.45f, w * 0.18f, h * 0.25f)
            }
            val right = Path().apply {
                moveTo(w * 0.5f, h * 0.55f)
                quadraticTo(w * 0.75f, h * 0.45f, w * 0.82f, h * 0.25f)
            }
            val top = Path().apply {
                moveTo(w * 0.5f, h * 0.45f)
                lineTo(w * 0.5f, h * 0.15f)
            }

            val brush = Brush.linearGradient(
                0f to tint.copy(alpha = 0.4f),
                1f to tint,
                start = Offset(0f, h),
                end = Offset(w, 0f)
            )
            drawPath(trunk, brush, style = stroke)
            drawPath(left, brush, style = stroke)
            drawPath(right, brush, style = stroke)
            drawPath(top, brush, style = stroke)

            val dotR = w * 0.06f
            drawCircle(tint, radius = dotR, center = Offset(w * 0.18f, h * 0.25f))
            drawCircle(tint, radius = dotR, center = Offset(w * 0.82f, h * 0.25f))
            drawCircle(tint, radius = dotR, center = Offset(w * 0.5f, h * 0.15f))

            drawCircle(
                tint.copy(alpha = 0.15f),
                radius = w * 0.48f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }
    }
}
