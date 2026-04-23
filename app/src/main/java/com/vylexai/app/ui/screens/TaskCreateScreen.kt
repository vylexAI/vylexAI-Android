@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val taskTypes = listOf(
    "Image classification",
    "Object detection",
    "NLP inference",
    "Moderation",
    "OCR",
    "Segmentation",
    "Speech recognition",
    "Audio classification",
    "Data validation",
    "Lightweight fine-tuning"
)

@Composable
fun TaskCreateScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "New task", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            var selected by remember { mutableStateOf(taskTypes.first()) }
            var name by remember { mutableStateOf("") }
            var model by remember { mutableStateOf("") }

            SectionTitle(text = "Type")
            Spacer(Modifier.height(10.dp))
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                taskTypes.forEach { t ->
                    FilterChip(
                        selected = selected == t,
                        onClick = { selected = t },
                        label = { Text(t) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VylexPalette.Cyan400.copy(alpha = 0.18f),
                            selectedLabelColor = VylexPalette.Cyan300,
                            containerColor = VylexPalette.Ink700,
                            labelColor = VylexPalette.Text300
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle(text = "Details")
            Spacer(Modifier.height(10.dp))
            VylexField(
                value = name,
                onValueChange = { name = it },
                label = "Task name"
            )
            Spacer(Modifier.height(12.dp))
            VylexField(
                value = model,
                onValueChange = { model = it },
                label = "Model (file or HuggingFace ref)"
            )
            Spacer(Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Dataset",
                        style = MaterialTheme.typography.labelLarge,
                        color = VylexPalette.Text500
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Drop files here or pick from storage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VylexPalette.Text300
                    )
                    Spacer(Modifier.height(12.dp))
                    GhostButton(text = "Choose files", onClick = {})
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle(text = "Parameters")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Batch size",
                            color = VylexPalette.Text500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "512",
                            color = VylexPalette.Text100,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Replication (N)",
                            color = VylexPalette.Text500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "3",
                            color = VylexPalette.Text100,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Deadline",
                            color = VylexPalette.Text500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "6 hours",
                            color = VylexPalette.Text100,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            PrimaryButton(text = "Submit · estimate 2.4 BSAI", onClick = {})
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun VylexField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VylexPalette.Text100,
            unfocusedTextColor = VylexPalette.Text100,
            focusedContainerColor = VylexPalette.Ink700,
            unfocusedContainerColor = VylexPalette.Ink700,
            focusedBorderColor = VylexPalette.Cyan400,
            unfocusedBorderColor = VylexPalette.Ink500,
            focusedLabelColor = VylexPalette.Cyan300,
            unfocusedLabelColor = VylexPalette.Text500
        )
    )
}

@Preview
@Composable
private fun TaskCreatePreview() {
    VylexTheme { VylexBackdrop { TaskCreateScreen(onBack = {}) } }
}
