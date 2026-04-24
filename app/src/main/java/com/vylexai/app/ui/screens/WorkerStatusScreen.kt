package com.vylexai.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.vylexai.app.ui.components.VylexTopBar
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

@Composable
fun WorkerStatusScreen(onBack: () -> Unit, viewModel: WorkerStatusViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "Worker", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            CurrentInferenceCard(state)
            Spacer(Modifier.height(16.dp))
            LatencyRow(state)
            Spacer(Modifier.height(16.dp))
            MetricsRow(state)
            Spacer(Modifier.height(24.dp))
            SectionTitle(text = "Latency (last 20)")
            Spacer(Modifier.height(10.dp))
            LatencyChart(state.latencyHistoryMs)
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Every bar represents one real on-device classification. No data leaves the phone.",
                style = MaterialTheme.typography.bodySmall,
                color = VylexPalette.Text500
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CurrentInferenceCard(state: WorkerUiState) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(
                    text = if (state.isRunning) "Running" else "Paused",
                    tone = if (state.isRunning) StatusTone.Active else StatusTone.Idle
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = state.currentSampleId?.let { "Sample · $it" } ?: "Warming up…",
                    style = MaterialTheme.typography.labelMedium,
                    color = VylexPalette.Text500
                )
            }
            Spacer(Modifier.height(14.dp))

            state.currentBitmap?.let { bmp ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            val top = state.lastResult?.top1
            Text(
                text = top?.label?.replaceFirstChar(Char::titlecase) ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                color = VylexPalette.Text100
            )
            Text(
                text = if (top != null) {
                    "%.1f%% confidence · MobileNetV1 quant · on-device".format(
                        top.confidence * PERCENT_100
                    )
                } else {
                    "MobileNetV1 quant · on-device"
                },
                style = MaterialTheme.typography.bodySmall,
                color = VylexPalette.Text500
            )
            if (state.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Inference stopped: ${state.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VylexPalette.Rose400
                )
            }
        }
    }
}

@Composable
private fun LatencyRow(state: WorkerUiState) {
    val latest = state.latencyHistoryMs.firstOrNull() ?: 0
    val average = if (state.latencyHistoryMs.isNotEmpty()) {
        state.latencyHistoryMs.average().toInt()
    } else {
        0
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile(
            label = "Last inference",
            value = "$latest",
            trailing = "ms",
            accent = VylexPalette.Cyan300,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Avg. (20)",
            value = "$average",
            trailing = "ms",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricsRow(state: WorkerUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile(
            label = "Tasks completed",
            value = state.completedTasks.toString(),
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "BSAI earned",
            value = "%.3f".format(state.rewardBsai),
            accent = VylexPalette.Emerald400,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LatencyChart(values: List<Int>) {
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT_DP.dp)
            .background(VylexPalette.Ink700, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (values.isEmpty()) return@Canvas
            val padding = 4f
            val slotWidth = size.width / HISTORY_CAPACITY
            val barWidth = slotWidth - padding
            values.reversed().forEachIndexed { i, v ->
                val h = size.height * (v.toFloat() / max)
                drawRect(
                    color = VylexPalette.Cyan400,
                    topLeft = Offset(i * slotWidth, size.height - h),
                    size = androidx.compose.ui.geometry.Size(barWidth, h)
                )
            }
        }
    }
}

private const val PERCENT_100 = 100
private const val CHART_HEIGHT_DP = 96
private const val HISTORY_CAPACITY = 20

@Preview
@Composable
private fun WorkerPreview() {
    VylexTheme { VylexBackdrop { /* runtime preview: needs Hilt VM, view in Studio */ } }
}
