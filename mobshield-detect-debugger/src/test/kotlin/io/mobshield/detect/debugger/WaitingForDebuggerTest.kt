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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WaitingForDebuggerTest {
    @Test
    fun scan_noDebugger_returnsNull() {
        assertNull(
            WaitingForDebugger.scan(
                waitingProvider = { false },
                connectedProvider = { false },
            ),
        )
    }

    @Test
    fun scan_debuggerConnected_returnsHit() {
        val hit =
            WaitingForDebugger.scan(
                waitingProvider = { false },
                connectedProvider = { true },
            )
        assertNotNull(hit)
        assertTrue(hit!!.connected)
    }
}
