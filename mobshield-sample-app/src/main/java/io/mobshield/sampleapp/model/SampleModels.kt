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

package io.mobshield.sampleapp.model

import io.mobshield.core.RiskLevel
import io.mobshield.core.Severity
import io.mobshield.core.Signal
import io.mobshield.core.ThreatEvent
import io.mobshield.core.ThreatType

enum class ModuleId(val label: String) {
    ROOT("Root / privileged access"),
    HOOKS("Hooks / instrumentation"),
    DEBUGGER("Debugger / ADB"),
    ENVIRONMENT("Emulator / automation"),
    INTEGRITY("App integrity"),
}

data class SamplePreferences(
    val detectOnly: Boolean = true,
    val allowDeveloperSignals: Boolean = true,
    val aggressiveBuildTime: Boolean = false,
    val enabledModules: Set<ModuleId> = ModuleId.entries.toSet(),
    val expectedPackageId: String = "io.mobshield.sampleapp",
)

data class ThreatCardUi(
    val id: String,
    val type: ThreatType,
    val severity: Severity,
    val score: Int,
    val signals: List<String>,
    val metadataSummary: String,
    val timestampMs: Long,
) {
    companion object {
        fun from(event: ThreatEvent): ThreatCardUi =
            ThreatCardUi(
                id = "${event.type.name}-${event.timestampMs}",
                type = event.type,
                severity = event.severity,
                score = event.score,
                signals = event.signals,
                metadataSummary =
                    event.metadata.entries.joinToString { "${it.key}=${it.value}" }
                        .ifEmpty { "no metadata" },
                timestampMs = event.timestampMs,
            )
    }
}

data class SignalRowUi(
    val name: String,
    val weight: Int,
    val confidence: Int,
    val evidenceSummary: String,
) {
    companion object {
        fun from(signal: Signal): SignalRowUi =
            SignalRowUi(
                name = signal.name,
                weight = signal.weight,
                confidence = signal.confidence,
                evidenceSummary =
                    signal.evidence.entries.joinToString { "${it.key}=${it.value}" }
                        .ifEmpty { "-" },
            )
    }
}

data class PostureUi(
    val riskLevel: RiskLevel,
    val running: Boolean,
    val activeThreatCount: Int,
    val signalSetVersion: String,
    val lastScanMs: Long,
)

data class MasvsRowUi(
    val control: String,
    val title: String,
    val coverage: String,
)
