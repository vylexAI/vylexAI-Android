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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.MetricTile
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.components.VylexTopBar
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun DeviceStateScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "Device state", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "Battery",
                    value = "87%",
                    accent = VylexPalette.Emerald400,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Temp",
                    value = "29°C",
                    accent = VylexPalette.Amber400,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "Network",
                    value = "412",
                    trailing = "Mbps",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Memory free",
                    value = "11.2",
                    trailing = "GB",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Compute targets")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    KV("CPU", "Tensor G4 · 8 cores")
                    KV("GPU", "Mali-G715 MP7")
                    KV("NPU / NNAPI", "Supported")
                    KV("Vulkan", "1.3 / supported", last = true)
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Runtime")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    KV("ONNX Runtime Mobile", "1.20.0")
                    KV("TensorFlow Lite", "2.16.1")
                    KV("Bonsai / 1-bit", "available Q4 2026", last = true)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun KV(k: String, v: String, last: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(k, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text500)
        Text(v, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text100)
    }
    if (!last) Spacer(Modifier.height(0.dp))
}

@Preview
@Composable
private fun DeviceStatePreview() {
    VylexTheme { VylexBackdrop { DeviceStateScreen(onBack = {}) } }
}
