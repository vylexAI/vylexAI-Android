package com.vylexai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vylexai.app.ui.components.GhostButton
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.components.VylexTopBar
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme
import java.math.BigDecimal

private const val USD_PER_BSAI = 7.69

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(onBack: () -> Unit, viewModel: WalletViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "Wallet", onBack = onBack)
        PullToRefreshBox(
            isRefreshing = state.kind == WalletKind.Loading,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                BalanceHero(state)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(text = "Claim", onClick = {}, modifier = Modifier.weight(1f))
                    GhostButton(text = "History", onClick = {}, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(28.dp))
                SectionTitle(text = "Activity")
                Spacer(Modifier.height(10.dp))
                ActivityList()
                Spacer(Modifier.height(8.dp))
                DemoFootnote(state.kind)
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun BalanceHero(state: WalletUiState) {
    Box(
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
                text = formatBsai(state.balanceBsai),
                style = MaterialTheme.typography.displayLarge,
                color = VylexPalette.Ink900
            )
            Text(
                text = "≈ \$${formatUsd(state.balanceBsai)}",
                style = MaterialTheme.typography.titleMedium,
                color = VylexPalette.Ink900.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (state.kind) {
                    WalletKind.Live -> "Held by VylexAI custody · withdrawals open at public beta"
                    WalletKind.Loading -> "Refreshing…"
                    WalletKind.Error -> state.message ?: "Couldn't reach the network"
                    WalletKind.Demo -> "Technical preview · simulated balance"
                },
                style = MaterialTheme.typography.labelMedium,
                color = VylexPalette.Ink900.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ActivityList() {
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
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VylexPalette.Text100
                    )
                    Text(
                        "Today · confirmed",
                        style = MaterialTheme.typography.labelSmall,
                        color = VylexPalette.Text500
                    )
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
}

@Composable
private fun DemoFootnote(kind: WalletKind) {
    if (kind != WalletKind.Demo) return
    Text(
        text = "Numbers shown while you're logged out come from a demo snapshot — log in to see your real balance.",
        style = MaterialTheme.typography.labelSmall,
        color = VylexPalette.Text500
    )
}

private fun formatBsai(amount: BigDecimal): String {
    val rounded = amount.setScale(2, java.math.RoundingMode.HALF_UP)
    return rounded.toPlainString()
}

private fun formatUsd(amount: BigDecimal): String {
    val usd = amount.multiply(BigDecimal(USD_PER_BSAI))
        .setScale(2, java.math.RoundingMode.HALF_UP)
    return usd.toPlainString()
}

@Preview
@Composable
private fun WalletPreview() {
    VylexTheme { VylexBackdrop { /* runtime preview needs Hilt — view in Studio */ } }
}
