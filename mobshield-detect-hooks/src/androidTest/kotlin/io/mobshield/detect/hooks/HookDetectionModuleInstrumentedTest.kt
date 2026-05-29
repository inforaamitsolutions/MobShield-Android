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

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.SignalAggregator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HookDetectionModuleInstrumentedTest {
    @Test
    fun scan_withoutFridaServer_noCriticalHookEvent() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val module = HookDetectionModule(context)
            val start = System.currentTimeMillis()
            val signals = module.scan()
            val elapsed = System.currentTimeMillis() - start
            assertTrue("scan took ${elapsed}ms", elapsed < 800L)

            val events = SignalAggregator(MobShieldConfig()).aggregate(signals)
            val criticalHooks =
                events.filter {
                    it.type == io.mobshield.core.ThreatType.HOOK_FRAMEWORK &&
                        it.severity == io.mobshield.core.Severity.CRITICAL
                }
            assertTrue(
                "unexpected critical hook events on clean emulator: $events signals=$signals",
                criticalHooks.isEmpty(),
            )
        }
}
