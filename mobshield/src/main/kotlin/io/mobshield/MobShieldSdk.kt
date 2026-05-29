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
import io.mobshield.core.MobShield
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.MobShieldListener
import io.mobshield.core.MobShieldState
import io.mobshield.detect.debugger.DebugDetectionRegistrar
import io.mobshield.detect.debugger.DebugSignalDefaults
import io.mobshield.detect.environment.EmulatorDetectionRegistrar
import io.mobshield.detect.environment.EmuSignalDefaults
import io.mobshield.detect.hooks.HookDetectionRegistrar
import io.mobshield.detect.hooks.HookSignalDefaults
import io.mobshield.detect.integrity.IntegrityDetectionRegistrar
import io.mobshield.detect.integrity.IntegritySignalDefaults
import io.mobshield.detect.root.RootDetectionRegistrar
import io.mobshield.detect.root.RootSignalDefaults

/**
 * Single entry point for the MobShield Android SDK (JitPack / umbrella artifact).
 *
 * All registrars, core APIs, and signal-default helpers are reachable through this object
 * when you depend on `com.github.<user>:<repo>:mobshield:<tag>`.
 */
object MobShieldSdk {
    /** Core runtime API (alias for discoverability in IDE). */
    @JvmStatic
    fun mobShield(): MobShield = MobShield

    /** Root / Magisk / Zygisk detection registration. */
    object Root {
        @JvmStatic
        fun register(
            context: Context,
            config: MobShieldConfig,
        ) = RootDetectionRegistrar.register(context, config)

        @JvmStatic
        fun signalDefaults(): RootSignalDefaults = RootSignalDefaults
    }

    /** Frida / Xposed / LSPosed hook detection registration. */
    object Hooks {
        @JvmStatic
        fun register(
            context: Context,
            config: MobShieldConfig,
        ) = HookDetectionRegistrar.register(context, config)

        @JvmStatic
        fun signalDefaults(): HookSignalDefaults = HookSignalDefaults
    }

    /** Debugger / ptrace / ADB detection registration. */
    object Debugger {
        @JvmStatic
        fun register(
            context: Context,
            config: MobShieldConfig,
        ) = DebugDetectionRegistrar.register(context, config)

        @JvmStatic
        fun signalDefaults(): DebugSignalDefaults = DebugSignalDefaults
    }

    /** Emulator / automation / environment detection registration. */
    object Environment {
        @JvmStatic
        fun register(
            context: Context,
            config: MobShieldConfig,
        ) = EmulatorDetectionRegistrar.register(context, config)

        @JvmStatic
        fun signalDefaults(): EmuSignalDefaults = EmuSignalDefaults
    }

    /** APK signature / installer integrity detection registration. */
    object Integrity {
        @JvmStatic
        fun register(
            context: Context,
            config: MobShieldConfig,
        ) = IntegrityDetectionRegistrar.register(context, config)

        @JvmStatic
        fun signalDefaults(): IntegritySignalDefaults = IntegritySignalDefaults
    }

    @JvmStatic
    fun registerAllDetectors(
        context: Context,
        config: MobShieldConfig,
    ) {
        Root.register(context, config)
        Hooks.register(context, config)
        Debugger.register(context, config)
        Environment.register(context, config)
        Integrity.register(context, config)
    }

    @JvmStatic
    fun start(
        context: Context,
        config: MobShieldConfig,
        listener: MobShieldListener,
    ) {
        MobShield.start(context, config, listener)
    }

    @JvmStatic
    fun stop() = MobShield.stop()

    @JvmStatic
    fun getState(): MobShieldState = MobShield.getState()
}
