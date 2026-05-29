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

import java.io.File

/** Scans common `su` locations without invoking a shell. */
object SuPathScan {
    private val staticCandidates =
        listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/cache/su",
            "/system/bin/.ext/su",
            "/system/usr/we-need-root/su",
        )

    fun scan(): ScanHit? {
        val discovered = LinkedHashSet<String>()
        collectExisting(staticCandidates, discovered)
        val pathEnv = System.getenv("PATH")
        if (pathEnv != null) {
            val pathCandidates =
                pathEnv.split(':').mapNotNull { dir ->
                    if (dir.isBlank()) {
                        null
                    } else if (dir.endsWith("/")) {
                        "${dir}su"
                    } else {
                        "$dir/su"
                    }
                }
            collectExisting(pathCandidates, discovered)
        }
        return firstHit(discovered)
    }

    internal fun scanCandidates(candidates: List<String>): ScanHit? {
        val discovered = LinkedHashSet<String>()
        collectExisting(candidates, discovered)
        return firstHit(discovered)
    }

    private fun collectExisting(
        paths: List<String>,
        out: LinkedHashSet<String>,
    ) {
        for (path in paths) {
            if (File(path).exists()) {
                out.add(path)
            }
        }
    }

    private fun firstHit(paths: Set<String>): ScanHit? {
        if (paths.isEmpty()) {
            return null
        }
        return ScanHit(paths.first())
    }

    data class ScanHit(val path: String)
}
