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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationFrameworkCheckTest {
    @Test
    fun scan_noAutomation_returnsNull() {
        val check =
            AutomationFrameworkCheck(
                runningServiceNames = { listOf("com.example.MyService") },
                accessibilityServiceIds = { emptyList() },
            )
        assertNull(check.scan())
    }

    @Test
    fun scan_appiumService_returnsHit() {
        val check =
            AutomationFrameworkCheck(
                runningServiceNames = { listOf("io.appium.settings.AppiumSettingsService") },
                accessibilityServiceIds = { emptyList() },
            )
        val hit = check.scan()
        assertNotNull(hit)
        assertTrue(hit!!.matches.any { it.contains("appium", ignoreCase = true) })
    }
}
