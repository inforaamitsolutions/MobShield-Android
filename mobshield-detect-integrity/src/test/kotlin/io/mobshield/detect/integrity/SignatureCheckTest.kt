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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SignatureCheckTest {
    private val expectedDigest =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    private val otherDigest =
        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
    private val mismatchDigest =
        "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"

    private val expected = listOf(expectedDigest)

    @Test
    fun scan_noExpectedSigners_returnsNull() {
        val check =
            SignatureCheck(
                packageName = "io.example",
                expectedSigners = emptyList(),
                certificateDigestsProvider = { listOf(otherDigest) },
            )
        assertNull(check.scan())
    }

    @Test
    fun scan_matchingDigest_returnsNull() {
        val check =
            SignatureCheck(
                packageName = "io.example",
                expectedSigners = expected,
                certificateDigestsProvider = { expected },
            )
        assertNull(check.scan())
    }

    @Test
    fun scan_mismatch_returnsHit() {
        val check =
            SignatureCheck(
                packageName = "io.example",
                expectedSigners = expected,
                certificateDigestsProvider = { listOf(mismatchDigest) },
            )
        assertNotNull(check.scan())
    }
}
