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

data class BuildSnapshot(
    val fingerprint: String,
    val manufacturer: String,
    val product: String,
    val model: String,
    val brand: String,
    val hardware: String,
)

class BuildFingerprintCheck(
    private val snapshotProvider: () -> BuildSnapshot = { defaultSnapshot() },
) {
    fun scan(): ScanHit? {
        val snapshot = snapshotProvider()
        val haystack =
            listOf(
                snapshot.fingerprint,
                snapshot.manufacturer,
                snapshot.product,
                snapshot.model,
                snapshot.brand,
                snapshot.hardware,
            ).joinToString("|") { it.lowercase() }

        val markers =
            listOf(
                "generic",
                "unknown",
                "emulator",
                "android sdk",
                "sdk_gphone",
                "google_sdk",
                "ranchu",
                "goldfish",
                "vbox",
                "genymotion",
            )

        val matched = markers.filter { haystack.contains(it) }
        if (matched.isEmpty()) {
            return null
        }
        return ScanHit(matched = matched, fingerprint = snapshot.fingerprint)
    }

    data class ScanHit(val matched: List<String>, val fingerprint: String)

    companion object {
        fun defaultSnapshot(): BuildSnapshot {
            val buildClass = Class.forName("android.os.Build")

            fun field(name: String): String {
                return try {
                    buildClass.getField(name).get(null)?.toString().orEmpty()
                } catch (_: Exception) {
                    ""
                }
            }
            return BuildSnapshot(
                fingerprint = field("FINGERPRINT"),
                manufacturer = field("MANUFACTURER"),
                product = field("PRODUCT"),
                model = field("MODEL"),
                brand = field("BRAND"),
                hardware = field("HARDWARE"),
            )
        }
    }
}
