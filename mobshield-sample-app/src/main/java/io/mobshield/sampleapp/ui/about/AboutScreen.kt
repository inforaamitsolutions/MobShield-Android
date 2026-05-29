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

package io.mobshield.sampleapp.ui.about

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.mobshield.sampleapp.viewmodel.SampleViewModel

@Composable
fun AboutScreen(viewModel: SampleViewModel) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("About MobShield", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SDK version ${viewModel.sdkVersion}")
                Text("Build entropy ${viewModel.buildEntropyPreview} (first 8 chars)")
                Text("Native self-check ${viewModel.nativeSelfCheck}")
                Text(
                    "Personalized native binaries are not reproducible across builds.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Text("MASVS-RESILIENCE coverage (summary)", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                viewModel.masvsRows.forEachIndexed { index, row ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    }
                    Text(row.control, fontFamily = FontFamily.Monospace)
                    Text(row.title, style = MaterialTheme.typography.bodyMedium)
                    Text("Coverage: ${row.coverage}", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
