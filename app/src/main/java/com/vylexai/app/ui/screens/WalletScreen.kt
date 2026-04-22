package com.vylexai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.GhostButton
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.components.VylexTopBar
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun WalletScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "Wallet", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VylexPalette.BrandGradient, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "BSAI balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = VylexPalette.Ink900.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "24.38",
                        style = MaterialTheme.typography.displayLarge,
                        color = VylexPalette.Ink900
                    )
                    Text(
                        text = "≈ \$187.40",
                        style = MaterialTheme.typography.titleMedium,
                        color = VylexPalette.Ink900.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Held by VylexAI custody · withdrawals open at public beta",
                        style = MaterialTheme.typography.labelMedium,
                        color = VylexPalette.Ink900.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(text = "Claim", onClick = {}, modifier = Modifier.weight(1f))
                GhostButton(text = "History", onClick = {}, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))
            SectionTitle(text = "Activity")
            Spacer(Modifier.height(10.dp))
            listOf(
                Triple("Network reward", "+0.018 BSAI", true),
                Triple("Network reward", "+0.023 BSAI", true),
                Triple("Task payment", "-0.41 BSAI", false),
                Triple("Network reward", "+0.019 BSAI", true)
            ).forEach { (label, amount, incoming) ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (incoming) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = if (incoming) VylexPalette.Emerald400 else VylexPalette.Cyan300
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text100)
                            Text("Today · confirmed", style = MaterialTheme.typography.labelSmall, color = VylexPalette.Text500)
                        }
                        Text(
                            amount,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (incoming) VylexPalette.Emerald400 else VylexPalette.Text100
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview
@Composable
private fun WalletPreview() {
    VylexTheme { VylexBackdrop { WalletScreen(onBack = {}) } }
}
