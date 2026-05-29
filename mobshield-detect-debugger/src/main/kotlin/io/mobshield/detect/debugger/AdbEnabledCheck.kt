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

import android.content.Context
import android.provider.Settings

class AdbEnabledCheck(
    private val settingsReader: (String) -> Int,
) {
    constructor(context: Context) : this(
        settingsReader = { key -> readGlobalInt(context, key) },
    )

    fun scan(): ScanHit? {
        val adb = settingsReader(Settings.Global.ADB_ENABLED) == 1
        val usbDebug = settingsReader("adb_usb_enabled") == 1
        if (!adb && !usbDebug) {
            return null
        }
        return ScanHit(adbEnabled = adb, usbDebugging = usbDebug)
    }

    data class ScanHit(val adbEnabled: Boolean, val usbDebugging: Boolean)

    companion object {
        fun readGlobalInt(
            context: Context,
            key: String,
        ): Int {
            return try {
                Settings.Global.getInt(context.contentResolver, key)
            } catch (_: Settings.SettingNotFoundException) {
                0
            }
        }
    }
}
