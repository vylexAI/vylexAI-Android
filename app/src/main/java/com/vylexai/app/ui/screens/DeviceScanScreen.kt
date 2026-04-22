package com.vylexai.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.BrandMark
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.MetricTile
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme
import kotlinx.coroutines.delay

private data class ScanReport(
    val model: String,
    val android: String,
    val cpu: String,
    val gpu: String,
    val ramGb: Int,
    val freeGb: Int,
    val battery: Int,
    val tempC: Int,
    val netMbps: Int,
    val netType: String,
    val nnapi: Boolean,
    val vulkan: Boolean,
    val score: Int,
    val estimateBsai: String
)

private val mockReport = ScanReport(
    model = "Pixel 9 Pro",
    android = "15",
    cpu = "Tensor G4 · 8 cores · 3.1 GHz",
    gpu = "Mali-G715 MP7",
    ramGb = 16,
    freeGb = 182,
    battery = 87,
    tempC = 29,
    netMbps = 412,
    netType = "Wi-Fi 6E",
    nnapi = true,
    vulkan = true,
    score = 842,
    estimateBsai = "$18 – $32 / month"
)

@Composable
fun DeviceScanScreen(onDone: () -> Unit) {
    var phase by remember { mutableStateOf(0) } // 0 = scanning, 1 = report

    LaunchedEffect(Unit) {
        delay(2200)
        phase = 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (phase == 0) {
            ScanningState()
        } else {
            ReportState(report = mockReport, onDone = onDone)
        }
    }
}

@Composable
private fun ColumnScope.ScanningState() {
    val rot = rememberInfiniteTransition(label = "scanRot")
    val a by rot.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "rot"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(220.dp).rotate(a),
            color = VylexPalette.Cyan300,
            strokeWidth = 2.dp
        )
        BrandMark(size = 120.dp)
    }
    Spacer(Modifier.height(24.dp))
    Text(
        text = "Scanning your device…",
        style = MaterialTheme.typography.headlineMedium,
        color = VylexPalette.Text100
    )
    Text(
        text = "Measuring CPU, NPU, memory, thermals and network. No data leaves your phone.",
        style = MaterialTheme.typography.bodyMedium,
        color = VylexPalette.Text500,
        modifier = Modifier.padding(top = 6.dp)
    )
}

private typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope

@Composable
private fun ReportState(report: ScanReport, onDone: () -> Unit) {
    Text(
        text = "Device profile",
        style = MaterialTheme.typography.displaySmall,
        color = VylexPalette.Text100
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "${report.model} · Android ${report.android}",
        style = MaterialTheme.typography.bodyLarge,
        color = VylexPalette.Text300
    )
    Spacer(Modifier.height(24.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricTile(
            label = "Performance score",
            value = report.score.toString(),
            trailing = "/ 1000",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Estimated BSAI",
            value = report.estimateBsai,
            accent = VylexPalette.Emerald400,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(16.dp))
    SectionTitle(text = "Hardware")
    Spacer(Modifier.height(10.dp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SpecRow("CPU", report.cpu)
            SpecRow("GPU", report.gpu)
            SpecRow("Memory", "${report.ramGb} GB")
            SpecRow("Free storage", "${report.freeGb} GB")
            SpecRow("NNAPI", if (report.nnapi) "available" else "—")
            SpecRow("Vulkan", if (report.vulkan) "available" else "—", last = true)
        }
    }
    Spacer(Modifier.height(16.dp))
    SectionTitle(text = "Live state")
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricTile(label = "Battery", value = "${report.battery}%", modifier = Modifier.weight(1f))
        MetricTile(label = "Temp", value = "${report.tempC}°C", modifier = Modifier.weight(1f))
        MetricTile(
            label = "Network",
            value = "${report.netMbps}",
            trailing = "Mbps",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(24.dp))
    PrimaryButton(text = "Enter Provider Dashboard", onClick = onDone)
}

@Composable
private fun SpecRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text500)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text100)
    }
    if (!last) Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 0.dp))
}

@Preview
@Composable
private fun DeviceScanPreview() {
    VylexTheme { VylexBackdrop { DeviceScanScreen(onDone = {}) } }
}
