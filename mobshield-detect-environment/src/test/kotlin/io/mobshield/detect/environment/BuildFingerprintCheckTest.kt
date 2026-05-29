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
package io.mobshield.detect.environment

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildFingerprintCheckTest {
    @Test
    fun scan_physicalDevice_returnsNull() {
        val check =
            BuildFingerprintCheck {
                BuildSnapshot(
                    fingerprint = "google/redfin/redfin:14/UPB4.230623.005",
                    manufacturer = "Google",
                    product = "redfin",
                    model = "Pixel 5",
                    brand = "google",
                    hardware = "redfin",
                )
            }
        assertNull(check.scan())
    }

    @Test
    fun scan_emulatorFingerprint_returnsHit() {
        val check =
            BuildFingerprintCheck {
                BuildSnapshot(
                    fingerprint = "google/sdk_gphone64_arm64/generic_arm64:14/...",
                    manufacturer = "Google",
                    product = "sdk_gphone64_arm64",
                    model = "sdk_gphone64_arm64",
                    brand = "google",
                    hardware = "ranchu",
                )
            }
        val hit = check.scan()
        assertNotNull(hit)
        assertTrue(hit!!.matched.isNotEmpty())
    }
}
