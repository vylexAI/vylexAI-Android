package com.vylexai.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vylexai.app.R
import com.vylexai.app.ui.theme.VylexPalette

/**
 * VylexAI tree-of-intelligence mark. Backed by the brand SVG traced from
 * vylexai.com (see brand/vylexai-logo.svg) — swap for the master vector
 * once the brand team delivers one.
 */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 96.dp, tint: Color = VylexPalette.Cyan300) {
    Image(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = "VylexAI",
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(tint)
    )
}
