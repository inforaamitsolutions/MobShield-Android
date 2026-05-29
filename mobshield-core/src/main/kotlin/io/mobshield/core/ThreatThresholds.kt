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

/** Per-threat score cutoffs from MOBSHIELD_SPEC section C.4. */
data class ThreatThreshold(
    val warning: Int,
    val critical: Int?,
) {
    init {
        require(warning in 0..100) { "warning must be in 0..100" }
        critical?.let { require(it in 0..100) { "critical must be in 0..100" } }
        critical?.let { require(warning <= it) { "warning must be <= critical" } }
    }
}

object DefaultThreatThresholds {
    val map: Map<ThreatType, ThreatThreshold> =
        mapOf(
            ThreatType.PRIVILEGED_ACCESS to ThreatThreshold(40, 70),
            ThreatType.HOOK_FRAMEWORK to ThreatThreshold(35, 65),
            ThreatType.DEBUGGER to ThreatThreshold(30, 60),
            ThreatType.EMULATOR to ThreatThreshold(25, 55),
            ThreatType.AUTOMATION to ThreatThreshold(30, 60),
            ThreatType.APP_INTEGRITY to ThreatThreshold(50, 80),
            ThreatType.DEVELOPER_MODE to ThreatThreshold(15, null),
            ThreatType.ADB_ENABLED to ThreatThreshold(15, null),
            ThreatType.UNOFFICIAL_STORE to ThreatThreshold(20, 50),
        )
}
