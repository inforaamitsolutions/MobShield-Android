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

class SensorCountCheck(
    private val sensorCountProvider: () -> Int,
    private val minimumPhysicalSensors: Int = DEFAULT_MINIMUM_SENSORS,
) {
    fun scan(): ScanHit? {
        val count = sensorCountProvider()
        if (count >= minimumPhysicalSensors) {
            return null
        }
        return ScanHit(count = count, threshold = minimumPhysicalSensors)
    }

    data class ScanHit(val count: Int, val threshold: Int)

    companion object {
        const val DEFAULT_MINIMUM_SENSORS = 7
    }
}
