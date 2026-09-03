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

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Builds and signs [ThreatReport]s. Pure and side-effect-free: it never performs networking — the
 * host app transmits the signed report to its backend.
 */
object MobShieldReporter {
    private const val HMAC_ALGORITHM = "HmacSHA256"

    /** Builds a report from a posture snapshot and the events of the last scan wave. */
    fun makeReport(
        state: MobShieldState,
        events: List<ThreatEvent>,
        buildId: String,
        schemaVersion: String = ThreatReport.CURRENT_SCHEMA_VERSION,
    ): ThreatReport {
        val entries =
            events
                .sortedByDescending { it.score }
                .map { event ->
                    ThreatReport.Entry(
                        type = event.type,
                        severity = event.severity,
                        score = event.score,
                        signals = event.signals,
                    )
                }
        return ThreatReport(
            schemaVersion = schemaVersion,
            signalSetVersion = state.signalSetVersion,
            buildId = buildId,
            generatedAtMs = state.lastScanMs,
            riskLevel = state.riskLevel,
            threats = entries,
        )
    }

    /**
     * Encodes a report to canonical (deterministic, sorted-key) JSON so a backend can recompute the
     * signature over an identical string. Built by hand to avoid Android-only JSON that is
     * unavailable in JVM unit tests.
     */
    fun canonicalJson(report: ThreatReport): String {
        val threats =
            report.threats.joinToString(prefix = "[", postfix = "]") { entry ->
                buildString {
                    append('{')
                    append("\"score\":").append(entry.score).append(',')
                    append("\"severity\":").append(quote(entry.severity.name)).append(',')
                    append("\"signals\":")
                    append(entry.signals.joinToString(prefix = "[", postfix = "]") { quote(it) })
                    append(',')
                    append("\"type\":").append(quote(entry.type.name))
                    append('}')
                }
            }
        return buildString {
            append('{')
            append("\"buildId\":").append(quote(report.buildId)).append(',')
            append("\"generatedAtMs\":").append(report.generatedAtMs).append(',')
            append("\"riskLevel\":").append(quote(report.riskLevel.name)).append(',')
            append("\"schemaVersion\":").append(quote(report.schemaVersion)).append(',')
            append("\"signalSetVersion\":").append(quote(report.signalSetVersion)).append(',')
            append("\"threats\":").append(threats)
            append('}')
        }
    }

    /** Produces the canonical JSON and its HMAC-SHA256 signature under [key]. */
    fun sign(
        report: ThreatReport,
        key: ByteArray,
    ): SignedThreatReport {
        val json = canonicalJson(report)
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        val signature = mac.doFinal(json.toByteArray(Charsets.UTF_8))
        return SignedThreatReport(json = json, signatureHex = signature.toHex())
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun quote(value: String): String {
        val sb = StringBuilder(value.length + 2)
        sb.append('"')
        for (ch in value) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else ->
                    if (ch < ' ') {
                        sb.append("\\u").append("%04x".format(ch.code))
                    } else {
                        sb.append(ch)
                    }
            }
        }
        sb.append('"')
        return sb.toString()
    }
}
