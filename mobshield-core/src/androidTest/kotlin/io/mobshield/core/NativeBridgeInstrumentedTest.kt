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

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeBridgeInstrumentedTest {
    @Test
    fun nativeLibrary_loadsAndSelfCheckNonZero() {
        val check = MobShield.selfCheck()
        assertNotEquals(0, check)
    }

    @Test
    fun version_matchesExpectedRelease() {
        val version = MobShield.getVersion()
        assertEquals("0.1.0", version)
    }

    @Test
    fun buildId_isNotEmpty() {
        val buildId = MobShield.getBuildId()
        assertTrue(buildId.isNotEmpty())
    }
}
