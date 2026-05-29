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
package io.mobshield.detect.debugger

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AdbEnabledCheckTest {
    @Test
    fun scan_adbDisabled_returnsNull() {
        val check = AdbEnabledCheck(settingsReader = { _ -> 0 })
        assertNull(check.scan())
    }

    @Test
    fun scan_adbEnabled_returnsHit() {
        val check =
            AdbEnabledCheck(
                settingsReader = { key ->
                    if (key == Settings.Global.ADB_ENABLED) 1 else 0
                },
            )
        val hit = check.scan()
        assertNotNull(hit)
        assertEquals(true, hit?.adbEnabled)
    }
}
