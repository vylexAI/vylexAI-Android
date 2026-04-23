package com.vylexai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.MetricTile
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.StatusChip
import com.vylexai.app.ui.components.StatusTone
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.components.VylexTopBar
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun WorkerStatusScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "Worker", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        StatusChip(text = "Running", tone = StatusTone.Active)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Task 0x7afc…3da2",
                            style = MaterialTheme.typography.labelMedium,
                            color = VylexPalette.Text500
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Image classification · ResNet-50",
                        style = MaterialTheme.typography.titleMedium,
                        color = VylexPalette.Text100
                    )
                    Text(
                        text = "batch 512 · 1.84 M FLOPs per item",
                        style = MaterialTheme.typography.bodySmall,
                        color = VylexPalette.Text500
                    )
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.64f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = VylexPalette.Cyan400,
                        trackColor = VylexPalette.Ink500,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text(
                            "64%",
                            style = MaterialTheme.typography.labelMedium,
                            color = VylexPalette.Cyan300
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "est. 1m 14s left",
                            style = MaterialTheme.typography.labelMedium,
                            color = VylexPalette.Text500
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(label = "CPU", value = "62%", modifier = Modifier.weight(1f))
                MetricTile(label = "Memory", value = "1.8 GB", modifier = Modifier.weight(1f))
                MetricTile(
                    label = "Temp",
                    value = "34°C",
                    accent = VylexPalette.Amber400,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "Network",
                    value = "31",
                    trailing = "Mbps",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Charge",
                    value = "83%",
                    accent = VylexPalette.Emerald400,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle(text = "Recent runs")
            Spacer(Modifier.height(10.dp))
            repeat(4) { i ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Task #${1284 - i}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VylexPalette.Text100
                            )
                            Text(
                                "Completed · 0.018 BSAI",
                                style = MaterialTheme.typography.labelMedium,
                                color = VylexPalette.Text500
                            )
                        }
                        Text(
                            "${12 + i}m ago",
                            style = MaterialTheme.typography.labelMedium,
                            color = VylexPalette.Text500
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
private fun WorkerPreview() {
    VylexTheme { VylexBackdrop { WorkerStatusScreen(onBack = {}) } }
}
