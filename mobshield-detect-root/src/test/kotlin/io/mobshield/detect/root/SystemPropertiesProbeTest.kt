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
package io.mobshield.detect.root

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SystemPropertiesProbeTest {
    @Test
    fun scan_secureProductionProps_returnsNull() {
        val hit =
            SystemPropertiesProbe.scan { key ->
                when (key) {
                    "ro.debuggable" -> "0"
                    "ro.secure" -> "1"
                    "ro.build.tags" -> "release-keys"
                    else -> ""
                }
            }
        assertNull(hit)
    }

    @Test
    fun scan_debuggableBuild_returnsHit() {
        val hit =
            SystemPropertiesProbe.scan { key ->
                when (key) {
                    "ro.debuggable" -> "1"
                    "ro.secure" -> "1"
                    "ro.build.tags" -> "release-keys"
                    else -> ""
                }
            }
        assertNotNull(hit)
        assertEquals("1", hit?.properties?.get("ro.debuggable"))
    }

    @Test
    fun scan_testKeysTag_returnsHit() {
        val hit =
            SystemPropertiesProbe.scan { key ->
                when (key) {
                    "ro.debuggable" -> "0"
                    "ro.secure" -> "1"
                    "ro.build.tags" -> "test-keys"
                    else -> ""
                }
            }
        assertNotNull(hit)
        assertEquals("test-keys", hit?.properties?.get("ro.build.tags"))
    }
}
