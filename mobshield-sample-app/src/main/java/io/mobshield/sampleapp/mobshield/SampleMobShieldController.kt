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

package io.mobshield.sampleapp.mobshield

import android.content.Context
import io.mobshield.core.MobShield
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.MobShieldListener
import io.mobshield.core.ModuleRegistry
import io.mobshield.core.Signal
import io.mobshield.core.TerminationPolicy
import io.mobshield.detect.debugger.DebugDetectionRegistrar
import io.mobshield.detect.environment.EmulatorDetectionRegistrar
import io.mobshield.detect.hooks.HookDetectionRegistrar
import io.mobshield.detect.integrity.IntegrityDetectionRegistrar
import io.mobshield.detect.root.RootDetectionRegistrar
import io.mobshield.sampleapp.model.ModuleId
import io.mobshield.sampleapp.model.SamplePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Registers detection modules and starts MobShield using [SamplePreferences].
 */
object SampleMobShieldController {
    fun buildConfig(prefs: SamplePreferences): MobShieldConfig {
        val builder =
            MobShieldConfig.builder()
                .expectedPackageId(prefs.expectedPackageId)
                .allowDeveloperSignals(prefs.allowDeveloperSignals)
                .allowedInstallSources(MobShieldConfig.DEFAULT_ALLOWED_INSTALL_SOURCES)

        if (prefs.detectOnly) {
            builder.detectOnly(true).terminationPolicy(TerminationPolicy.NONE)
        } else {
            builder
                .detectOnly(false)
                .terminationPolicy(TerminationPolicy.EXIT_ON_CRITICAL)
        }
        return builder.build()
    }

    fun registerModules(
        context: Context,
        config: MobShieldConfig,
        prefs: SamplePreferences,
    ) {
        ModuleRegistry.clear()
        if (prefs.enabledModules.contains(ModuleId.ROOT)) {
            RootDetectionRegistrar.register(context, config)
        }
        if (prefs.enabledModules.contains(ModuleId.HOOKS)) {
            HookDetectionRegistrar.register(context, config)
        }
        if (prefs.enabledModules.contains(ModuleId.DEBUGGER)) {
            DebugDetectionRegistrar.register(context, config)
        }
        if (prefs.enabledModules.contains(ModuleId.ENVIRONMENT)) {
            EmulatorDetectionRegistrar.register(context, config)
        }
        if (prefs.enabledModules.contains(ModuleId.INTEGRITY)) {
            IntegrityDetectionRegistrar.register(context, config)
        }
    }

    fun start(
        context: Context,
        prefs: SamplePreferences,
        listener: MobShieldListener,
    ) {
        val config = buildConfig(prefs)
        registerModules(context.applicationContext, config, prefs)
        MobShield.start(context.applicationContext, config, listener)
    }

    fun stop() {
        MobShield.stop()
    }

    fun restart(
        context: Context,
        prefs: SamplePreferences,
        listener: MobShieldListener,
    ) {
        stop()
        start(context, prefs, listener)
    }

    suspend fun collectSignals(
        context: Context,
        prefs: SamplePreferences,
    ): List<Signal> =
        withContext(Dispatchers.Default) {
            val config = buildConfig(prefs)
            registerModules(context.applicationContext, config, prefs)
            val modules = ModuleRegistry.getAll()
            modules.flatMap { module ->
                runCatching { module.scan() }.getOrElse { emptyList() }
            }
        }

    fun buildEntropyPreview(): String {
        val buildId = MobShield.getBuildId()
        return buildId.take(8).ifEmpty { "--------" }
    }
}
