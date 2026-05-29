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
package io.mobshield.core

import android.content.Context
import io.mobshield.core.internal.MobShieldEngine
import io.mobshield.core.internal.NativeBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicReference

/** Public entry point for MobShield on Android. */
object MobShield {
    const val SIGNAL_SET_VERSION = "signals-2026.05.0"

    private val engineRef = AtomicReference<MobShieldEngine?>(null)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Starts MobShield: loads native core, schedules registered [DetectionModule] scans,
     * and delivers callbacks on the listener.
     */
    @JvmStatic
    fun start(
        context: Context,
        config: MobShieldConfig,
        listener: MobShieldListener,
    ) {
        context.applicationContext
        NativeBridge.ensureLoaded()
        stop()
        val engine =
            MobShieldEngine(
                config = config,
                listener = listener,
                resolveModules = { ModuleRegistry.getAll() },
                scope = appScope,
                signalSetVersion = SIGNAL_SET_VERSION,
            )
        engineRef.set(engine)
        engine.start()
    }

    /** Stops active scans and resets runtime state. */
    @JvmStatic
    fun stop() {
        engineRef.getAndSet(null)?.stop()
    }

    /** Returns the latest posture snapshot. */
    @JvmStatic
    fun getState(): MobShieldState {
        return engineRef.get()?.getState()
            ?: MobShieldState(
                riskLevel = RiskLevel.NONE,
                activeThreats = emptyList(),
                lastScanMs = 0L,
                signalSetVersion = SIGNAL_SET_VERSION,
                running = false,
            )
    }

    /** Native and API version string. */
    @JvmStatic
    fun getVersion(): String {
        NativeBridge.ensureLoaded()
        return NativeBridge.getVersion()
    }

    /** Native build identifier injected at compile time. */
    @JvmStatic
    fun getBuildId(): String {
        NativeBridge.ensureLoaded()
        return NativeBridge.getBuildId()
    }

    /** Native integrity self-check; nonzero indicates a healthy core. */
    @JvmStatic
    fun selfCheck(): Int {
        NativeBridge.ensureLoaded()
        return NativeBridge.selfCheck()
    }

    internal fun resetForTests() {
        stop()
        ModuleRegistry.clear()
    }
}
