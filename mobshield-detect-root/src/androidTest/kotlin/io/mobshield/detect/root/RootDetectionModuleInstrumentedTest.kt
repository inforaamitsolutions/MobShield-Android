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

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RootDetectionModuleInstrumentedTest {
    @Test
    fun scan_completesWithin500ms_onCleanDevice() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val module = RootDetectionModule(context)
            val start = System.currentTimeMillis()
            val signals = module.scan()
            val elapsed = System.currentTimeMillis() - start
            assertTrue("scan took ${elapsed}ms", elapsed < 500L)
            // Clean Pixel emulator should not exceed aggregate critical threshold from root alone
            val totalWeight = signals.sumOf { it.weight }
            assertTrue("unexpected high root score on clean emulator: $signals", totalWeight < 120)
        }
}
