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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MobShieldReporterTest {
    private val key = "test-shared-secret".toByteArray()

    @Test
    fun makeReport_mapsStateAndEvents_sortedByScoreDescending() {
        val report =
            MobShieldReporter.makeReport(
                state = state(RiskLevel.HIGH, lastScanMs = 1234L),
                events =
                    listOf(
                        event(ThreatType.DEBUGGER, Severity.MEDIUM, 40),
                        event(ThreatType.PRIVILEGED_ACCESS, Severity.CRITICAL, 90),
                    ),
                buildId = "android-test-abc",
            )

        assertEquals(ThreatReport.CURRENT_SCHEMA_VERSION, report.schemaVersion)
        assertEquals("android-test-abc", report.buildId)
        assertEquals(1234L, report.generatedAtMs)
        assertEquals(RiskLevel.HIGH, report.riskLevel)
        assertEquals(
            listOf(ThreatType.PRIVILEGED_ACCESS, ThreatType.DEBUGGER),
            report.threats.map { it.type },
        )
        assertEquals(90, report.threats.first().score)
    }

    @Test
    fun canonicalJson_isDeterministicAndSortedKeys() {
        val report =
            MobShieldReporter.makeReport(
                state = state(RiskLevel.MEDIUM, lastScanMs = 7L),
                events = listOf(event(ThreatType.HOOK_FRAMEWORK, Severity.HIGH, 70)),
                buildId = "b",
            )
        val a = MobShieldReporter.canonicalJson(report)
        val b = MobShieldReporter.canonicalJson(report)
        assertEquals(a, b)
        assertTrue(a.contains("\"schemaVersion\":\"report-1\""))
        // buildId key precedes riskLevel key (alphabetical order).
        assertTrue(a.indexOf("\"buildId\"") < a.indexOf("\"riskLevel\""))
    }

    @Test
    fun sign_matchesIndependentlyComputedHmac() {
        val report = MobShieldReporter.makeReport(state(RiskLevel.LOW, 1L), emptyList(), "b")
        val signed = MobShieldReporter.sign(report, key)

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        val expected = mac.doFinal(signed.json.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        assertEquals(expected, signed.signatureHex)
    }

    @Test
    fun sign_tamperedJson_failsVerification() {
        val report =
            MobShieldReporter.makeReport(
                state(RiskLevel.HIGH, 1L),
                listOf(event(ThreatType.PRIVILEGED_ACCESS, Severity.CRITICAL, 95)),
                "b",
            )
        val signed = MobShieldReporter.sign(report, key)

        val tampered = signed.json.replace("\"HIGH\"", "\"NONE\"")
        assertNotEquals(signed.json, tampered)

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        val recomputed = mac.doFinal(tampered.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        assertNotEquals(signed.signatureHex, recomputed)
    }

    @Test
    fun sign_differentKey_producesDifferentSignature() {
        val report = MobShieldReporter.makeReport(state(RiskLevel.LOW, 1L), emptyList(), "b")
        val a = MobShieldReporter.sign(report, "key-a".toByteArray())
        val b = MobShieldReporter.sign(report, "key-b".toByteArray())
        assertEquals(a.json, b.json)
        assertNotEquals(a.signatureHex, b.signatureHex)
    }

    private fun state(
        risk: RiskLevel,
        lastScanMs: Long,
    ): MobShieldState =
        MobShieldState(
            riskLevel = risk,
            activeThreats = emptyList(),
            lastScanMs = lastScanMs,
            signalSetVersion = MobShield.SIGNAL_SET_VERSION,
            running = true,
        )

    private fun event(
        type: ThreatType,
        severity: Severity,
        score: Int,
    ): ThreatEvent =
        when (type) {
            ThreatType.PRIVILEGED_ACCESS ->
                ThreatEvent.PrivilegedAccess(severity, listOf("${type.name}.mock"), score, 0L)
            ThreatType.HOOK_FRAMEWORK ->
                ThreatEvent.HookFramework(severity, listOf("${type.name}.mock"), score, 0L)
            ThreatType.DEBUGGER ->
                ThreatEvent.Debugger(severity, listOf("${type.name}.mock"), score, 0L)
            else ->
                ThreatEvent.AppIntegrity(severity, listOf("${type.name}.mock"), score, 0L)
        }
}
