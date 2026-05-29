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

import java.io.File

/**
 * Compares expected linker library names with entries visible in `/proc/self/maps`.
 */
class SuspiciousLibraryScan(
    private val mapsReader: () -> String? = { readMapsSafely() },
    private val libraryNames: List<String> = DEFAULT_LIBRARIES,
) {
    fun scan(): ScanHit? {
        val maps = mapsReader() ?: return null
        for (library in libraryNames) {
            val mappedName = System.mapLibraryName(library)
            val token = library.lowercase()
            for (line in maps.lineSequence()) {
                val lower = line.lowercase()
                if (!lower.contains(token) && !lower.contains(mappedName.lowercase())) {
                    continue
                }
                if (lower.contains("frida") || lower.contains("xposed") || lower.contains("lsposed")) {
                    return ScanHit(line.trim())
                }
            }
        }
        return null
    }

    data class ScanHit(val library: String)

    companion object {
        val DEFAULT_LIBRARIES =
            listOf(
                "frida-gadget",
                "xposed",
                "substrate",
            )

        fun readMapsSafely(): String? {
            return try {
                File("/proc/self/maps").readText()
            } catch (_: Exception) {
                null
            }
        }
    }
}
