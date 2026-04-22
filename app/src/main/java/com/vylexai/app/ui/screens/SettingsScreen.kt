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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.GlassCard
import com.vylexai.app.ui.components.SectionTitle
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.components.VylexTopBar
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        VylexTopBar(title = "Settings", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SectionTitle("Worker")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ToggleRow("Only while charging", true)
                    ToggleRow("Only on Wi-Fi", true)
                    ToggleRow("Auto-start at boot", false)
                    ToggleRow("Run on cellular if unmetered", false)
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionTitle("Limits")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    var battery by remember { mutableFloatStateOf(40f) }
                    var temp by remember { mutableFloatStateOf(40f) }
                    var cpu by remember { mutableFloatStateOf(70f) }
                    SliderRow("Min battery", "${battery.roundToInt()}%", battery, 10f..100f) { battery = it }
                    SliderRow("Max temperature", "${temp.roundToInt()}°C", temp, 30f..50f) { temp = it }
                    SliderRow("CPU load ceiling", "${cpu.roundToInt()}%", cpu, 20f..100f) { cpu = it }
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionTitle("Notifications")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ToggleRow("Earnings confirmed", true)
                    ToggleRow("Task assigned", false)
                    ToggleRow("Network status alerts", true)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ToggleRow(label: String, initial: Boolean) {
    var v by remember { mutableStateOf(initial) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = VylexPalette.Text100,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = v,
            onCheckedChange = { v = it },
            colors = SwitchDefaults.colors(
                checkedTrackColor = VylexPalette.Cyan400,
                checkedThumbColor = VylexPalette.Ink900,
                uncheckedTrackColor = VylexPalette.Ink500
            )
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: String,
    v: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text500, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = VylexPalette.Text100)
        }
        Slider(
            value = v,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = VylexPalette.Cyan300,
                activeTrackColor = VylexPalette.Cyan400,
                inactiveTrackColor = VylexPalette.Ink500
            )
        )
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    VylexTheme { VylexBackdrop { SettingsScreen(onBack = {}) } }
}
