/*
 * Copyright 2025 MobShield Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mobshield.sampleapp.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.mobshield.sampleapp.model.ModuleId
import io.mobshield.sampleapp.viewmodel.SampleViewModel

@Composable
fun ConfigScreen(viewModel: SampleViewModel) {
    val prefs by viewModel.prefs.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Configuration", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Changes apply on the next Start or Rescan. Fail-closed uses EXIT_ON_CRITICAL.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Response policy", style = MaterialTheme.typography.titleMedium)
                ToggleRow(
                    label = "Detect-only (no termination)",
                    checked = prefs.detectOnly,
                    onCheckedChange = viewModel::setDetectOnly,
                )
                ToggleRow(
                    label = "Allow developer / ADB signals",
                    checked = prefs.allowDeveloperSignals,
                    onCheckedChange = viewModel::setAllowDeveloperSignals,
                )
                HorizontalDivider()
                Text(
                    text =
                        "Aggressive anti-debug is configured at build time via " +
                            "mobshield { aggressive = true } in build.gradle.kts.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Detection modules", style = MaterialTheme.typography.titleMedium)
                ModuleId.entries.forEach { module ->
                    ToggleRow(
                        label = module.label,
                        checked = prefs.enabledModules.contains(module),
                        onCheckedChange = { enabled -> viewModel.toggleModule(module, enabled) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
