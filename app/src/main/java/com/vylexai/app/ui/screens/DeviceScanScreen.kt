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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vylexai.app.domain.device.DeviceReport
import com.vylexai.app.ui.components.BrandMark
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.MetricTile
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun DeviceScanScreen(onDone: () -> Unit, viewModel: DeviceScanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        when (val s = state) {
            is DeviceScanState.Scanning -> ScanningState()
            is DeviceScanState.Ready -> ReportState(report = s.report, onDone = onDone)
            is DeviceScanState.Error -> ErrorState(message = s.message, onRetry = viewModel::rescan)
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
            modifier = Modifier
                .size(220.dp)
                .rotate(a),
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

@Composable
private fun ColumnScope.ErrorState(message: String, onRetry: () -> Unit) {
    Text(
        text = "Couldn't scan this device",
        style = MaterialTheme.typography.displaySmall,
        color = VylexPalette.Text100
    )
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = VylexPalette.Text300,
        modifier = Modifier.padding(top = 8.dp)
    )
    Spacer(Modifier.height(20.dp))
    PrimaryButton(text = "Try again", onClick = onRetry)
}

@Composable
private fun ReportState(report: DeviceReport, onDone: () -> Unit) {
    Text(
        text = "Device profile",
        style = MaterialTheme.typography.displaySmall,
        color = VylexPalette.Text100
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "${report.model} · Android ${report.androidSdk}",
        style = MaterialTheme.typography.bodyLarge,
        color = VylexPalette.Text300
    )
    Spacer(Modifier.height(24.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricTile(
            label = "Performance score",
            value = report.performanceScore.toString(),
            trailing = "/ 1000",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Estimated BSAI",
            value = "${report.estimateMonthlyBsai.first}–${report.estimateMonthlyBsai.second}",
            trailing = "/ mo",
            accent = VylexPalette.Emerald400,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(16.dp))
    SectionTitle(text = "Hardware")
    Spacer(Modifier.height(10.dp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SpecRow(
                "CPU",
                buildString {
                    append(report.cpu.model ?: "—")
                    append(" · ${report.cpu.cores} cores")
                    report.cpu.maxFrequencyMhz?.let { append(" · ${it / 1000f} GHz") }
                }
            )
            SpecRow("GPU", report.gpu ?: "—")
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
        MetricTile(
            label = "Battery",
            value = "${report.battery.percent}%",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Temp",
            value = "${report.battery.temperatureC}°C",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Network",
            value = report.network.downlinkMbps?.toString() ?: "—",
            trailing = "Mbps",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(24.dp))
    PrimaryButton(text = "Enter Provider Dashboard", onClick = onDone)
    Spacer(Modifier.height(24.dp))
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
    if (!last) Box(modifier = Modifier.fillMaxWidth().height(1.dp))
}

@Preview
@Composable
private fun DeviceScanPreview() {
    VylexTheme { VylexBackdrop { /* live preview needs Hilt; use Studio preview at runtime */ } }
}
