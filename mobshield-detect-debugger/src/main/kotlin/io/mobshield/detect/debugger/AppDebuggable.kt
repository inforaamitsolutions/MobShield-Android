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
import android.content.pm.ApplicationInfo

class AppDebuggable(
    private val isDebuggable: () -> Boolean,
    private val isReleaseBuild: () -> Boolean,
) {
    constructor(context: Context) : this(
        isDebuggable = { isDebuggableFlagSet(context) },
        isReleaseBuild = { isReleaseVariant(context) },
    )

    fun scan(): ScanHit? {
        if (!isDebuggable()) {
            return null
        }
        return ScanHit(isReleaseBuild = isReleaseBuild())
    }

    data class ScanHit(val isReleaseBuild: Boolean)

    companion object {
        fun isDebuggableFlagSet(context: Context): Boolean {
            return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }

        fun isReleaseVariant(context: Context): Boolean {
            return try {
                val buildConfig = Class.forName("${context.packageName}.BuildConfig")
                !buildConfig.getField("DEBUG").getBoolean(null)
            } catch (_: Exception) {
                !isDebuggableFlagSet(context)
            }
        }
    }
}
