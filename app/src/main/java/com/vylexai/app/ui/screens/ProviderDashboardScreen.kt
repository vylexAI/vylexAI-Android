package com.vylexai.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.MetricTile
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.StatusChip
import com.vylexai.app.ui.components.StatusTone
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun ProviderDashboardScreen(
    onOpenWorker: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenDevice: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var active by remember { mutableStateOf(true) }
    val dash by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Provider",
                    style = MaterialTheme.typography.labelLarge,
                    color = VylexPalette.Cyan300
                )
                Text(
                    text = "Good evening",
                    style = MaterialTheme.typography.displaySmall,
                    color = VylexPalette.Text100
                )
            }
            IconButton(onClick = onOpenWallet) {
                Icon(Icons.Rounded.AccountBalanceWallet, null, tint = VylexPalette.Text100)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, null, tint = VylexPalette.Text100)
            }
        }

        Spacer(Modifier.height(20.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        text = if (active) "Active" else "Paused",
                        tone = if (active) StatusTone.Active else StatusTone.Idle
                    )
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = VylexPalette.Cyan400,
                            checkedThumbColor = VylexPalette.Ink900,
                            uncheckedTrackColor = VylexPalette.Ink500
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Your node is serving the network.",
                    style = MaterialTheme.typography.titleMedium,
                    color = VylexPalette.Text100
                )
                Text(
                    text = "Tasks run only while charging on Wi-Fi with a safe temperature.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VylexPalette.Text500,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "BSAI earned (30d)",
                value = formatBsaiAmount(dash.bsaiEarned),
                accent = VylexPalette.Emerald400,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Tasks completed",
                value = formatInt(dash.tasksCompleted),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "Contribution score",
                value = dash.contributionScore.toString(),
                trailing = "/ 100",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Thermal",
                value = "29°C",
                accent = VylexPalette.Amber400,
                modifier = Modifier.weight(1f)
            )
        }
        if (dash.kind == DashboardKind.Demo) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Demo figures — sign in to see your real contribution.",
                style = MaterialTheme.typography.labelSmall,
                color = VylexPalette.Text500
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle(text = "Network")
        Spacer(Modifier.height(12.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                KeyValue("Nodes online", "2.41 M")
                KeyValue("Current task", "Image classification · batch 512")
                KeyValue("Avg. latency", "412 ms", last = true)
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickLink(
                label = "Worker",
                icon = Icons.Rounded.Memory,
                onClick = onOpenWorker,
                modifier = Modifier.weight(1f)
            )
            QuickLink(
                label = "Device state",
                icon = Icons.Rounded.Memory,
                onClick = onOpenDevice,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun KeyValue(k: String, v: String, last: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(k, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text500)
        Text(v, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text100)
    }
    if (!last) Spacer(Modifier.height(0.dp))
}

@Composable
private fun QuickLink(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = VylexPalette.Cyan300)
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = VylexPalette.Text100
            )
            Text(
                text = "Open",
                style = MaterialTheme.typography.labelMedium,
                color = VylexPalette.Cyan300
            )
        }
    }
}

@Preview
@Composable
private fun ProviderPreview() {
    VylexTheme {
        VylexBackdrop {
            ProviderDashboardScreen({}, {}, {}, {})
        }
    }
}
