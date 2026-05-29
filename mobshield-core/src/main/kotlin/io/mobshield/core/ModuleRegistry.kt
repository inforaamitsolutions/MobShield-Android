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

import java.util.concurrent.CopyOnWriteArrayList

/** Thread-safe registry of [DetectionModule] instances. */
object ModuleRegistry {
    private val modules = CopyOnWriteArrayList<DetectionModule>()

    fun register(module: DetectionModule) {
        require(module.name.isNotBlank()) { "module name must not be blank" }
        modules.removeAll { it.name == module.name }
        modules.add(module)
    }

    fun getAll(): List<DetectionModule> = modules.sortedByDescending { it.criticality }

    fun clear() {
        modules.clear()
    }
}
