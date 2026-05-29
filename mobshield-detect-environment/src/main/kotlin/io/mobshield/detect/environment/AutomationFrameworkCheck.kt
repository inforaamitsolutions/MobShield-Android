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

class AutomationFrameworkCheck(
    private val runningServiceNames: () -> List<String> = { emptyList() },
    private val accessibilityServiceIds: () -> List<String> = { emptyList() },
) {
    fun scan(): ScanHit? {
        val matches = LinkedHashSet<String>()
        for (name in runningServiceNames()) {
            val lower = name.lowercase()
            if (AUTOMATION_SERVICE_MARKERS.any { lower.contains(it) }) {
                matches.add(name)
            }
        }
        for (id in accessibilityServiceIds()) {
            val lower = id.lowercase()
            if (AUTOMATION_ACCESSIBILITY_MARKERS.any { lower.contains(it) }) {
                matches.add(id)
            }
        }
        if (matches.isEmpty()) {
            return null
        }
        return ScanHit(matches.toList())
    }

    data class ScanHit(val matches: List<String>)

    companion object {
        val AUTOMATION_SERVICE_MARKERS =
            listOf(
                "io.appium",
                "appium",
                "uiautomator",
                "androidx.test",
                "espresso",
            )

        val AUTOMATION_ACCESSIBILITY_MARKERS =
            listOf(
                "io.appium",
                "uiautomator",
                "accessibility.uiautomator",
            )
    }
}
