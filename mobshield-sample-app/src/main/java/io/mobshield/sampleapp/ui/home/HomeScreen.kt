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

package io.mobshield.sampleapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.mobshield.sampleapp.model.ThreatCardUi
import io.mobshield.sampleapp.ui.components.severityColor
import io.mobshield.sampleapp.viewmodel.SampleViewModel

@Composable
fun HomeScreen(viewModel: SampleViewModel) {
    val threats by viewModel.threats.collectAsState()
    val posture by viewModel.posture.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Threat events", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Risk ${posture.riskLevel.name} | Running ${posture.running} | Active ${posture.activeThreatCount}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.startMobShield() }) { Text("Start") }
            OutlinedButton(onClick = { viewModel.stopMobShield() }) { Text("Stop") }
            OutlinedButton(onClick = { viewModel.rescan() }) { Text("Rescan") }
            OutlinedButton(onClick = { viewModel.clearThreats() }) { Text("Clear") }
        }

        if (threats.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No threats yet", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Start MobShield to run all enabled detection modules.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(threats, key = { it.id }) { card ->
                    ThreatEventCard(card)
                }
            }
        }
    }
}

@Composable
private fun ThreatEventCard(card: ThreatCardUi) {
    val color = severityColor(card.severity)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(card.type.name, fontWeight = FontWeight.Bold)
                Surface(color = color.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                    Text(
                        card.severity.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = color,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text("Score ${card.score}", style = MaterialTheme.typography.titleMedium)
            Text(
                card.signals.joinToString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                card.metadataSummary,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
