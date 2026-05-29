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

import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning

object RootSignalDefaults {
    const val MOUNT_NAMESPACE = "android.root.mount_namespace"
    const val MAGISK_UDS = "android.root.magisk_uds"
    const val OVERLAYFS = "android.root.overlayfs"
    const val ERRNO_DEVIATION = "android.root.errno_deviation"
    const val ZYGISK_MAPS = "android.root.zygisk_maps"
    const val KERNELSU = "android.root.kernelsu_sysfs"
    const val PATH_PROBE = "android.root.path_probe"
    const val DANGEROUS_PACKAGES = "android.root.dangerous_packages"
    const val PROPS = "android.root.props"

    private val defaults: Map<String, SignalTuning> =
        mapOf(
            MOUNT_NAMESPACE to SignalTuning(weight = 75, confidence = 85),
            MAGISK_UDS to SignalTuning(weight = 70, confidence = 80),
            OVERLAYFS to SignalTuning(weight = 65, confidence = 75),
            ERRNO_DEVIATION to SignalTuning(weight = 60, confidence = 70),
            ZYGISK_MAPS to SignalTuning(weight = 70, confidence = 80),
            KERNELSU to SignalTuning(weight = 75, confidence = 85),
            PATH_PROBE to SignalTuning(weight = 25, confidence = 35),
            DANGEROUS_PACKAGES to SignalTuning(weight = 55, confidence = 70),
            PROPS to SignalTuning(weight = 20, confidence = 30),
        )

    fun buildSignal(
        name: String,
        evidence: Map<String, String>,
        tuning: Map<String, SignalTuning>,
    ): Signal {
        val resolved = tuning[name] ?: defaults[name] ?: SignalTuning(weight = 30, confidence = 40)
        return Signal(
            name = name,
            weight = resolved.weight,
            confidence = resolved.confidence,
            evidence = evidence,
        )
    }
}
