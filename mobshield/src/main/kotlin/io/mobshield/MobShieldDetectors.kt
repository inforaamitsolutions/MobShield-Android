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
package io.mobshield

import android.content.Context
import io.mobshield.core.MobShieldConfig
import io.mobshield.detect.debugger.DebugDetectionRegistrar
import io.mobshield.detect.environment.EmulatorDetectionRegistrar
import io.mobshield.detect.hooks.HookDetectionRegistrar
import io.mobshield.detect.integrity.IntegrityDetectionRegistrar
import io.mobshield.detect.root.RootDetectionRegistrar

/**
 * Top-level aliases in [io.mobshield] for detector registrars.
 *
 * Use when importing `io.mobshield.detect.*` does not resolve in the IDE, or prefer a single package:
 * ```
 * import io.mobshield.MobShieldDetectors.root
 * MobShieldDetectors.root.register(context, config)
 * ```
 *
 * The canonical types remain [RootDetectionRegistrar], [HookDetectionRegistrar], etc.
 */
object MobShieldDetectors {
    /** @see RootDetectionRegistrar */
    val root: RootDetectionRegistrar get() = RootDetectionRegistrar

    /** @see HookDetectionRegistrar */
    val hooks: HookDetectionRegistrar get() = HookDetectionRegistrar

    /** @see DebugDetectionRegistrar */
    val debugger: DebugDetectionRegistrar get() = DebugDetectionRegistrar

    /** @see EmulatorDetectionRegistrar */
    val environment: EmulatorDetectionRegistrar get() = EmulatorDetectionRegistrar

    /** @see IntegrityDetectionRegistrar */
    val integrity: IntegrityDetectionRegistrar get() = IntegrityDetectionRegistrar

    @JvmStatic
    fun registerAll(
        context: Context,
        config: MobShieldConfig,
    ) {
        RootDetectionRegistrar.register(context, config)
        HookDetectionRegistrar.register(context, config)
        DebugDetectionRegistrar.register(context, config)
        EmulatorDetectionRegistrar.register(context, config)
        IntegrityDetectionRegistrar.register(context, config)
    }
}
