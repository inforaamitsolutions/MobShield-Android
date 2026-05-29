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
import org.junit.Assert.assertTrue
import org.junit.Test

class MobShieldConfigTest {
    @Test
    fun defaults_detectOnlyWithNoTermination() {
        val config = MobShieldConfig()
        assertTrue(config.detectOnly)
        assertEquals(TerminationPolicy.NONE, config.terminationPolicy)
        assertTrue(config.allowDeveloperSignals)
        assertEquals(DefaultThreatThresholds.map, config.thresholds)
    }

    @Test
    fun builder_producesEquivalentConfig() {
        val signer = "a".repeat(64)
        val built =
            MobShieldConfig.builder()
                .detectOnly(false)
                .expectedSigners(listOf(signer))
                .terminationPolicy(TerminationPolicy.EXIT_ON_CRITICAL)
                .allowDeveloperSignals(false)
                .expectedPackageId("io.example.app")
                .periodicIntervalSec(60)
                .build()
        assertEquals(false, built.detectOnly)
        assertEquals(listOf(signer), built.expectedSigners)
        assertEquals(TerminationPolicy.EXIT_ON_CRITICAL, built.terminationPolicy)
        assertEquals("io.example.app", built.expectedPackageId)
        assertEquals(60, built.periodicIntervalSec)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidSigner_rejected() {
        MobShieldConfig(expectedSigners = listOf("not-hex"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun detectOnly_withTermination_rejected() {
        MobShieldConfig(
            detectOnly = true,
            terminationPolicy = TerminationPolicy.EXIT_ON_CRITICAL,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun periodicInterval_mustBePositive() {
        MobShieldConfig(periodicIntervalSec = 0)
    }

    @Test
    fun validateSigners_acceptsUppercaseHex() {
        val signer = "A".repeat(64)
        MobShieldConfig.validateSigners(listOf(signer))
    }

    @Test(expected = IllegalArgumentException::class)
    fun threatThreshold_warningAboveCritical_rejected() {
        ThreatThreshold(warning = 80, critical = 50)
    }
}
