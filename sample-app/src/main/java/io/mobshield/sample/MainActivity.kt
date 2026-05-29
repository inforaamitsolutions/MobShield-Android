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
package io.mobshield.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import io.mobshield.core.MobShield
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.MobShieldListener
import io.mobshield.core.ThreatEvent
import io.mobshield.detect.debugger.DebugDetectionRegistrar
import io.mobshield.detect.environment.EmulatorDetectionRegistrar
import io.mobshield.detect.hooks.HookDetectionRegistrar
import io.mobshield.detect.integrity.IntegrityDetectionRegistrar
import io.mobshield.detect.root.RootDetectionRegistrar

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = MobShieldConfig()
        RootDetectionRegistrar.register(this, config)
        HookDetectionRegistrar.register(this, config)
        DebugDetectionRegistrar.register(this, config)
        EmulatorDetectionRegistrar.register(this, config)
        IntegrityDetectionRegistrar.register(this, config)
        MobShield.start(
            this,
            config,
            object : MobShieldListener {
                override fun onThreat(event: ThreatEvent) = Unit

                override fun onAllChecksFinished(events: List<ThreatEvent>) = Unit
            },
        )
        val tv = TextView(this)
        tv.text = "MobShield " + MobShield.getVersion()
        setContentView(tv)
    }
}
