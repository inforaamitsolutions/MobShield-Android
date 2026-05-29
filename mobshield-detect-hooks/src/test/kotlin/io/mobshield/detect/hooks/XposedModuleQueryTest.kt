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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class XposedModuleQueryTest {
    @Test
    fun scan_missingBridge_returnsNull() {
        val query = XposedModuleQuery(ClassLoader.getSystemClassLoader())
        assertNull(query.scan())
    }

    @Test
    fun scan_presentBridge_returnsHit() {
        val hit =
            XposedModuleQuery(
                classLoader = ClassLoader.getSystemClassLoader(),
                canLoadClass = { it == "de.robv.android.xposed.XposedBridge" },
            ).scan()
        assertNotNull(hit)
        assertEquals("de.robv.android.xposed.XposedBridge", hit?.className)
    }
}
