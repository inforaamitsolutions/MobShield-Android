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
package io.mobshield.core

/**
 * Atomic weighted observation from a single detection probe.
 *
 * @param name Stable dot-notation identifier (for example `android.root.mount_namespace`).
 * @param weight Importance if the condition holds, in range 0..100.
 * @param confidence Detector certainty in range 0..100 (maps to spec multipliers at aggregate time).
 * @param evidence Optional non-PII diagnostic key-value pairs.
 * @param timestampMs Epoch millis when the signal was produced.
 */
data class Signal(
    val name: String,
    val weight: Int,
    val confidence: Int,
    val evidence: Map<String, String> = emptyMap(),
    val timestampMs: Long = System.currentTimeMillis(),
) {
    init {
        require(name.isNotBlank()) { "signal name must not be blank" }
        require(weight in 0..100) { "weight must be in 0..100" }
        require(confidence in 0..100) { "confidence must be in 0..100" }
    }
}
