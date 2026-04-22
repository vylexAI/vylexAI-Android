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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.MetricTile
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.StatusChip
import com.vylexai.app.ui.components.StatusTone
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme

private data class JobRow(val title: String, val model: String, val state: String, val tone: StatusTone, val progress: String)

private val mockJobs = listOf(
    JobRow("Product photo tagging", "resnet50.onnx", "Running", StatusTone.Active, "64%"),
    JobRow("Support ticket moderation", "distilbert.tflite", "Queued", StatusTone.Idle, "—"),
    JobRow("Dataset OCR pass", "trocr.onnx", "Done", StatusTone.Active, "100%")
)

@Composable
fun ClientDashboardScreen(
    onNewTask: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Client",
                    style = MaterialTheme.typography.labelLarge,
                    color = VylexPalette.Cyan300
                )
                Text(
                    text = "Your AI lab",
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "Active jobs",
                value = "2",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "This month spend",
                value = "8.14",
                trailing = "BSAI",
                accent = VylexPalette.Cyan300,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))

        PrimaryButton(text = "New task", onClick = onNewTask, leading = Icons.Rounded.Add)

        Spacer(Modifier.height(28.dp))
        SectionTitle(text = "Jobs")
        Spacer(Modifier.height(12.dp))

        mockJobs.forEach { j ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = j.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = VylexPalette.Text100,
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(text = j.state, tone = j.tone)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text(
                            text = j.model,
                            style = MaterialTheme.typography.bodySmall,
                            color = VylexPalette.Text500,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = j.progress,
                            style = MaterialTheme.typography.bodySmall,
                            color = VylexPalette.Cyan300
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Preview
@Composable
private fun ClientPreview() {
    VylexTheme { VylexBackdrop { ClientDashboardScreen({}, {}, {}) } }
}
