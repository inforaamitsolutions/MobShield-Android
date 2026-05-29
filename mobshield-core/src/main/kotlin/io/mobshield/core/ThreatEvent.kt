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

/** User-visible aggregated threat outcome. */
sealed class ThreatEvent {
    abstract val type: ThreatType
    abstract val severity: Severity
    abstract val signals: List<String>
    abstract val score: Int
    abstract val timestampMs: Long
    abstract val metadata: Map<String, String>

    data class PrivilegedAccess(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.PRIVILEGED_ACCESS
    }

    data class HookFramework(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.HOOK_FRAMEWORK
    }

    data class Debugger(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.DEBUGGER
    }

    data class Emulator(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.EMULATOR
    }

    data class Automation(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.AUTOMATION
    }

    data class AppIntegrity(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.APP_INTEGRITY
    }

    data class DeveloperMode(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.DEVELOPER_MODE
    }

    data class AdbEnabled(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.ADB_ENABLED
    }

    data class UnofficialStore(
        override val severity: Severity,
        override val signals: List<String>,
        override val score: Int,
        override val timestampMs: Long,
        override val metadata: Map<String, String> = emptyMap(),
    ) : ThreatEvent() {
        override val type: ThreatType = ThreatType.UNOFFICIAL_STORE
    }

    companion object {
        fun create(
            type: ThreatType,
            severity: Severity,
            signals: List<String>,
            score: Int,
            timestampMs: Long,
            metadata: Map<String, String> = emptyMap(),
        ): ThreatEvent =
            when (type) {
                ThreatType.PRIVILEGED_ACCESS ->
                    PrivilegedAccess(severity, signals, score, timestampMs, metadata)
                ThreatType.HOOK_FRAMEWORK ->
                    HookFramework(severity, signals, score, timestampMs, metadata)
                ThreatType.DEBUGGER ->
                    Debugger(severity, signals, score, timestampMs, metadata)
                ThreatType.EMULATOR ->
                    Emulator(severity, signals, score, timestampMs, metadata)
                ThreatType.AUTOMATION ->
                    Automation(severity, signals, score, timestampMs, metadata)
                ThreatType.APP_INTEGRITY ->
                    AppIntegrity(severity, signals, score, timestampMs, metadata)
                ThreatType.DEVELOPER_MODE ->
                    DeveloperMode(severity, signals, score, timestampMs, metadata)
                ThreatType.ADB_ENABLED ->
                    AdbEnabled(severity, signals, score, timestampMs, metadata)
                ThreatType.UNOFFICIAL_STORE ->
                    UnofficialStore(severity, signals, score, timestampMs, metadata)
            }
    }
}
