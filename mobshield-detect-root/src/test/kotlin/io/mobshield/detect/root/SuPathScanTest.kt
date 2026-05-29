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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SuPathScanTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun scanCandidates_missingSu_returnsNull() {
        val missing = File(tempFolder.root, "definitely-missing-su").absolutePath
        assertNull(SuPathScan.scanCandidates(listOf(missing)))
    }

    @Test
    fun scanCandidates_existingSu_returnsPath() {
        val su = File(tempFolder.root, "su")
        assertEquals(true, su.createNewFile())
        val hit = SuPathScan.scanCandidates(listOf(su.absolutePath))
        assertNotNull(hit)
        assertEquals(su.absolutePath, hit?.path)
    }
}
