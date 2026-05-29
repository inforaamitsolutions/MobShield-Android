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
package io.mobshield.detect.integrity

import android.os.Build
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ApkChecksumCheckTest {
    private val digest = "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"

    @Test
    fun scan_noExpected_returnsNull() {
        val check = ApkChecksumCheck(expectedApkSha256 = null, checksumProvider = { digest })
        assertNull(check.scan())
    }

    @Test
    fun scan_matchingChecksum_returnsNull() {
        val check =
            ApkChecksumCheck(
                expectedApkSha256 = digest,
                sdkIntProvider = { Build.VERSION_CODES.S },
                checksumProvider = { digest },
            )
        assertNull(check.scan())
    }

    @Test
    fun scan_mismatch_returnsHit() {
        val check =
            ApkChecksumCheck(
                expectedApkSha256 = digest,
                sdkIntProvider = { Build.VERSION_CODES.S },
                checksumProvider = { "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" },
            )
        assertNotNull(check.scan())
    }
}
