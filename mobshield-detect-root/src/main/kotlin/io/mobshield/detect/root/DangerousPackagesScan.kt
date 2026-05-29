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

import android.content.pm.PackageManager

/** Detects installed root and hook manager packages. */
class DangerousPackagesScan(
    private val isPackageInstalled: (String) -> Boolean,
) {
    constructor(packageManager: PackageManager) : this(
        isPackageInstalled = { packageName ->
            try {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        },
    )

    fun scan(): ScanHit? {
        for (entry in knownPackages) {
            if (isPackageInstalled(entry.packageName)) {
                return ScanHit(entry.packageName, entry.label)
            }
        }
        return null
    }

    data class ScanHit(
        val packageName: String,
        val label: String,
    )

    companion object {
        private data class KnownPackage(val packageName: String, val label: String)

        private val knownPackages =
            listOf(
                KnownPackage("com.topjohnwu.magisk", "Magisk"),
                KnownPackage("io.github.huskydg.magisk", "Magisk"),
                KnownPackage("me.weishu.kernelsu", "KernelSU"),
                KnownPackage("com.kernelsu.manager", "KernelSU Manager"),
                KnownPackage("de.robv.android.xposed.installer", "Xposed Installer"),
                KnownPackage("org.lsposed.manager", "LSPosed"),
                KnownPackage("com.saurik.substrate", "Cydia Substrate"),
            )
    }
}
