package com.vylexai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.navigation.Mode
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun ModeSelectScreen(onSelect: (Mode) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 64.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "How will you join the network?",
            style = MaterialTheme.typography.displaySmall,
            color = VylexPalette.Text100
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You can switch at any time in Settings.",
            style = MaterialTheme.typography.bodyLarge,
            color = VylexPalette.Text500
        )
        Spacer(Modifier.height(40.dp))

        ModeCard(
            icon = Icons.Rounded.Bolt,
            title = "Provider",
            tagline = "Lend compute, earn BSAI",
            body = "Your device joins the network as a node. When it's charging and on Wi-Fi, we route lightweight AI tasks to it and pay you in BSAI.",
            onClick = { onSelect(Mode.Provider) }
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            icon = Icons.Rounded.AutoAwesome,
            title = "Client",
            tagline = "Run AI on the network",
            body = "Submit inference, classification, OCR and fine-tuning jobs. The network dispatches them across millions of nodes at a fraction of cloud cost.",
            onClick = { onSelect(Mode.Client) }
        )
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    tagline: String,
    body: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .height(44.dp)
                        .background(
                            VylexPalette.Cyan400.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = VylexPalette.Cyan300)
                }
                Spacer(Modifier.height(0.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.height(0.dp))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = VylexPalette.Text100
                    )
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.labelMedium,
                        color = VylexPalette.Cyan300
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = VylexPalette.Text300
            )
        }
    }
}

@Preview
@Composable
private fun ModeSelectPreview() {
    VylexTheme { VylexBackdrop { ModeSelectScreen(onSelect = {}) } }
}
