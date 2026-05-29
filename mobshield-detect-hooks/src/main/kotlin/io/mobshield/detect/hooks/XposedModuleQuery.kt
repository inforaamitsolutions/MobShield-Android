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

/**
 * Attempts to load known Xposed / LSPosed bridge classes through the app [ClassLoader].
 */
class XposedModuleQuery(
    private val classLoader: ClassLoader,
    private val classNames: List<String> = DEFAULT_CLASS_NAMES,
    private val canLoadClass: (String) -> Boolean = { className -> tryLoad(classLoader, className) },
) {
    fun scan(): ScanHit? {
        for (className in classNames) {
            if (canLoadClass(className)) {
                return ScanHit(className)
            }
        }
        return null
    }

    companion object {
        val DEFAULT_CLASS_NAMES =
            listOf(
                "de.robv.android.xposed.XposedBridge",
                "org.lsposed.lspd.core.Main",
                "io.github.lsposed.lspd.core.Main",
            )

        fun tryLoad(
            classLoader: ClassLoader,
            className: String,
        ): Boolean {
            return try {
                Class.forName(className, false, classLoader)
                true
            } catch (_: ClassNotFoundException) {
                false
            } catch (_: LinkageError) {
                false
            }
        }
    }

    data class ScanHit(val className: String)
}
