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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Files

class NativeLibChecksumCheckTest {
    private val digest = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"

    @Test
    fun scan_matchingFile_returnsNull() {
        val temp = Files.createTempFile("libmobshieldcore", ".so").toFile()
        temp.writeBytes(byteArrayOf(1, 2, 3))
        val check =
            NativeLibChecksumCheck(
                expectedNativeLibSha256 = digest,
                libraryFileProvider = { temp },
                digestComputer = { digest },
            )
        assertNull(check.scan())
        temp.delete()
    }

    @Test
    fun scan_mismatch_returnsHit() {
        val temp = Files.createTempFile("libmobshieldcore", ".so").toFile()
        val check =
            NativeLibChecksumCheck(
                expectedNativeLibSha256 = digest,
                libraryFileProvider = { temp },
                digestComputer = { "abababababababababababababababababababababababababababababababab" },
            )
        assertNotNull(check.scan())
        temp.delete()
    }

    @Test
    fun coreLibraryFile_resolvesUnderNativeDir() {
        val file = NativeLibChecksumCheck.coreLibraryFile("/data/app/lib")
        assertNotNull(file)
        assertEquals("libmobshieldcore.so", file?.name)
    }
}
