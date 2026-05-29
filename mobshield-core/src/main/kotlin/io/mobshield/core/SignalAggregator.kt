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
package io.mobshield.core

/**
 * Stateless pure aggregator: maps weighted [Signal] values to [ThreatEvent] list.
 *
 * Score formula per threat type T:
 * `score(T) = min(100, sum(weight * confidence / 100))` for signals mapped to T.
 */
class SignalAggregator(
    private val config: MobShieldConfig,
) {
    fun aggregate(signals: List<Signal>): List<ThreatEvent> {
        if (signals.isEmpty()) {
            return emptyList()
        }

        val grouped = LinkedHashMap<ThreatType, MutableList<Signal>>()
        for (signal in signals) {
            val type = resolveThreatType(signal.name) ?: continue
            if (!config.allowDeveloperSignals &&
                (type == ThreatType.DEVELOPER_MODE || type == ThreatType.ADB_ENABLED)
            ) {
                continue
            }
            grouped.getOrPut(type) { mutableListOf() }.add(signal)
        }

        val now = System.currentTimeMillis()
        val events = ArrayList<ThreatEvent>(grouped.size)
        for ((type, typeSignals) in grouped) {
            val score = computeScore(typeSignals)
            if (score <= 0) {
                continue
            }
            val threshold = config.thresholds[type] ?: DefaultThreatThresholds.map.getValue(type)
            val severity = mapSeverity(type, score, threshold)
            if (severity == Severity.INFO && score < threshold.warning / 2) {
                continue
            }
            val names = typeSignals.map { it.name }
            val metadata = mergeEvidence(typeSignals)
            events.add(
                ThreatEvent.create(
                    type = type,
                    severity = severity,
                    signals = names,
                    score = score,
                    timestampMs = now,
                    metadata = metadata,
                ),
            )
        }
        return events.sortedByDescending { it.score }
    }

    private fun computeScore(signals: List<Signal>): Int {
        var total = 0.0
        for (signal in signals) {
            total += signal.weight * (signal.confidence / 100.0)
        }
        return total.toInt().coerceIn(0, 100)
    }

    private fun mapSeverity(
        type: ThreatType,
        score: Int,
        threshold: ThreatThreshold,
    ): Severity {
        val criticalCap = threshold.critical == null
        if (criticalCap && type in INFORMATIONAL_TYPES) {
            return when {
                score < threshold.warning -> Severity.INFO
                else -> Severity.HIGH
            }
        }

        val critical = threshold.critical
        if (critical != null && score >= critical) {
            return Severity.CRITICAL
        }
        if (score >= threshold.warning) {
            return Severity.HIGH
        }
        if (score >= threshold.warning / 2) {
            return Severity.MEDIUM
        }
        if (score >= threshold.warning / 4) {
            return Severity.LOW
        }
        return Severity.INFO
    }

    private fun mergeEvidence(signals: List<Signal>): Map<String, String> {
        val merged = LinkedHashMap<String, String>()
        for (signal in signals) {
            for ((key, value) in signal.evidence) {
                merged["${signal.name}.$key"] = value
            }
        }
        return merged
    }

    companion object {
        private val INFORMATIONAL_TYPES =
            setOf(ThreatType.DEVELOPER_MODE, ThreatType.ADB_ENABLED)

        fun resolveThreatType(signalName: String): ThreatType? {
            val name = signalName.lowercase()
            return when {
                name.startsWith("android.root.") || name.startsWith("ios.jb.") ->
                    ThreatType.PRIVILEGED_ACCESS
                name.contains(".hook.") || name.startsWith("common.hook.") -> ThreatType.HOOK_FRAMEWORK
                name.startsWith("android.debug.") || name.startsWith("ios.debug.") ->
                    ThreatType.DEBUGGER
                name.startsWith("android.env.") || name.startsWith("ios.env.") ->
                    ThreatType.EMULATOR
                name.startsWith("android.automation.") || name.startsWith("ios.automation.") ->
                    ThreatType.AUTOMATION
                name.startsWith("android.integrity.") || name.startsWith("ios.integrity.") ->
                    ThreatType.APP_INTEGRITY
                name.startsWith("android.store.") -> ThreatType.UNOFFICIAL_STORE
                name.startsWith("android.dev.") || name.endsWith(".developer_mode") ->
                    ThreatType.DEVELOPER_MODE
                name.startsWith("android.adb.") || name.endsWith(".adb_enabled") ->
                    ThreatType.ADB_ENABLED
                else -> null
            }
        }
    }
}
