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

import io.mobshield.detect.root.internal.RootNativeBridge

/**
 * Reads selected `ro.*` properties via native `__system_property_get` (no Kotlin reflection).
 */
object SystemPropertiesProbe {
    fun scan(propertyReader: (String) -> String = RootNativeBridge::readSystemProperty): ScanHit? {
        val debuggable = propertyReader("ro.debuggable")
        val secure = propertyReader("ro.secure")
        val tags = propertyReader("ro.build.tags")

        val anomalies = LinkedHashMap<String, String>()
        if (debuggable == "1") {
            anomalies["ro.debuggable"] = debuggable
        }
        if (secure == "0") {
            anomalies["ro.secure"] = secure
        }
        if (tags.contains("test-keys")) {
            anomalies["ro.build.tags"] = tags
        }
        if (anomalies.isEmpty()) {
            return null
        }
        return ScanHit(anomalies)
    }

    data class ScanHit(val properties: Map<String, String>)
}
