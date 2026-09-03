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

/** The outcome of running one [DetectionModule.scan]. */
data class ModuleValidationResult(
    val moduleName: String,
    val criticality: Int,
    val signals: List<Signal>,
) {
    /** Whether the module emitted at least one signal this run. */
    val didFire: Boolean get() = signals.isNotEmpty()
}

/**
 * Structured result of a validation run: the raw per-module signals *and* the aggregated
 * [ThreatEvent] list they map to. Use this to see exactly which detectors fired in a given
 * environment (clean device, rooted device, device with Frida attached, ...) and how the signals
 * aggregate into threats.
 *
 * Unlike the engine (which scans concurrently and only surfaces aggregated events to a listener),
 * this preserves per-module attribution and runs modules in criticality order.
 */
data class ValidationReport(
    val modules: List<ModuleValidationResult>,
    val events: List<ThreatEvent>,
    val generatedAtMs: Long,
) {
    /** Every signal emitted across all modules, in module (criticality) order. */
    val rawSignals: List<Signal> get() = modules.flatMap { it.signals }

    /** The set of distinct signal names that fired this run. */
    val firedSignalNames: Set<String> get() = rawSignals.map { it.name }.toSet()

    /** The set of distinct threat types raised after aggregation. */
    val activeThreatTypes: Set<ThreatType> get() = events.map { it.type }.toSet()

    /** The most severe aggregated threat this run, or null when nothing fired. */
    val highestSeverity: Severity? get() = events.maxByOrNull { it.severity }?.severity
}

/**
 * Runs the SDK's detection modules and reports exactly what fired.
 *
 * This is the entry point for on-device and CI validation: register (or hand in) the detection
 * modules, run them against the current environment, and inspect the [ValidationReport] to confirm
 * the expected signals appear (on a compromised device) or stay silent (on a clean one).
 */
object MobShieldValidation {
    /**
     * Scans [modules] (in descending [DetectionModule.criticality] order), aggregates their signals
     * with [config], and returns a structured [ValidationReport].
     */
    suspend fun run(
        config: MobShieldConfig = MobShieldConfig(),
        modules: List<DetectionModule>,
    ): ValidationReport {
        val ordered = modules.sortedByDescending { it.criticality }
        val moduleResults = mutableListOf<ModuleValidationResult>()
        val allSignals = mutableListOf<Signal>()
        for (module in ordered) {
            val signals = runCatching { module.scan() }.getOrElse { emptyList() }
            allSignals += signals
            moduleResults += ModuleValidationResult(module.name, module.criticality, signals)
        }
        val events = SignalAggregator(config).aggregate(allSignals)
        return ValidationReport(moduleResults, events, System.currentTimeMillis())
    }

    /**
     * Convenience: runs whatever modules are currently registered in [ModuleRegistry]. Register
     * modules via the per-module registrars first.
     */
    suspend fun runRegistered(config: MobShieldConfig = MobShieldConfig()): ValidationReport =
        run(config, ModuleRegistry.getAll())
}
