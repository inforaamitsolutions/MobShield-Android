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
package io.mobshield.detect.hooks

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SuspiciousLibraryScanTest {
    @Test
    fun scan_cleanMaps_returnsNull() {
        val scan =
            SuspiciousLibraryScan(
                mapsReader = { readResource("proc/maps_clean.txt") },
                libraryNames = listOf("frida-gadget"),
            )
        assertNull(scan.scan())
    }

    @Test
    fun scan_fridaMaps_returnsHit() {
        val scan =
            SuspiciousLibraryScan(
                mapsReader = { readResource("proc/maps_frida.txt") },
                libraryNames = listOf("frida-gadget"),
            )
        val hit = scan.scan()
        assertNotNull(hit)
    }

    @Test
    fun scan_restrictedProc_returnsNull() {
        val scan = SuspiciousLibraryScan(mapsReader = { null })
        assertNull(scan.scan())
    }

    private fun readResource(path: String): String {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream(path))
        return stream.bufferedReader().use { it.readText() }
    }
}
