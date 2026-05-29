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
 * Scans live thread stack traces for known instrumentation frames.
 */
object ClassNameLeakScan {
    private val suspiciousPrefixes =
        listOf(
            "de.robv.android.xposed",
            "org.lsposed",
            "io.github.lsposed",
            "com.saurik.substrate",
            "frida",
            "re.frida",
        )

    fun scan(stackProvider: () -> Map<Thread, Array<StackTraceElement>> = { Thread.getAllStackTraces() }): ScanHit? {
        val stacks = stackProvider()
        for ((_, trace) in stacks) {
            for (element in trace) {
                val className = element.className
                val match = suspiciousPrefixes.firstOrNull { className.startsWith(it) }
                if (match != null) {
                    return ScanHit(className, element.methodName)
                }
            }
        }
        return null
    }

    data class ScanHit(
        val className: String,
        val methodName: String,
    )
}
