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
 * A serializable snapshot of the current posture, intended to be sent to a backend for server-side
 * decisioning. Client-side termination is bypassable, so the authoritative decision belongs on a
 * server that receives and verifies this report.
 */
data class ThreatReport(
    val schemaVersion: String,
    val signalSetVersion: String,
    val buildId: String,
    val generatedAtMs: Long,
    val riskLevel: RiskLevel,
    val threats: List<Entry>,
) {
    data class Entry(
        val type: ThreatType,
        val severity: Severity,
        val score: Int,
        val signals: List<String>,
    )

    companion object {
        const val CURRENT_SCHEMA_VERSION = "report-1"
    }
}

/**
 * A [ThreatReport] serialized to canonical JSON together with an HMAC-SHA256 signature over exactly
 * those bytes. Send [json] and [signatureHex] to the backend; the backend recomputes the HMAC over
 * the received [json] with the shared key and rejects the report if it does not match.
 */
data class SignedThreatReport(
    /** Canonical JSON of the report — the exact string the signature covers (UTF-8). */
    val json: String,
    /** Lowercase hex HMAC-SHA256 of [json]. */
    val signatureHex: String,
)
