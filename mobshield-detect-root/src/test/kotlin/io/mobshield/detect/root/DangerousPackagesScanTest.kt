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

class DangerousPackagesScanTest {
    @Test
    fun scan_noPackagesInstalled_returnsNull() {
        val scan = DangerousPackagesScan(isPackageInstalled = { false })
        assertNull(scan.scan())
    }

    @Test
    fun scan_magiskInstalled_returnsHit() {
        val scan =
            DangerousPackagesScan(
                isPackageInstalled = { packageName -> packageName == "com.topjohnwu.magisk" },
            )
        val hit = scan.scan()
        assertNotNull(hit)
        assertEquals("com.topjohnwu.magisk", hit?.packageName)
        assertEquals("Magisk", hit?.label)
    }

    @Test
    fun scan_lsposedInstalled_returnsHit() {
        val scan =
            DangerousPackagesScan(
                isPackageInstalled = { packageName -> packageName == "org.lsposed.manager" },
            )
        val hit = scan.scan()
        assertNotNull(hit)
        assertEquals("org.lsposed.manager", hit?.packageName)
    }
}
