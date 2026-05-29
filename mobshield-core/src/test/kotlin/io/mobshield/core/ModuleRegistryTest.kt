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

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuleRegistryTest {
    @After
    fun tearDown() {
        ModuleRegistry.clear()
    }

    @Test
    fun register_andGetAll_sortedByCriticality() {
        ModuleRegistry.register(module("low", 1))
        ModuleRegistry.register(module("high", 99))
        assertEquals("high", ModuleRegistry.getAll().first().name)
    }

    @Test
    fun register_replacesSameName() {
        ModuleRegistry.register(module("dup", 1))
        ModuleRegistry.register(module("dup", 5))
        assertEquals(1, ModuleRegistry.getAll().size)
        assertEquals(5, ModuleRegistry.getAll().first().criticality)
    }

    @Test
    fun clear_removesAll() {
        ModuleRegistry.register(module("a", 1))
        ModuleRegistry.clear()
        assertEquals(0, ModuleRegistry.getAll().size)
    }

    private fun module(
        name: String,
        criticality: Int,
    ): DetectionModule =
        object : DetectionModule {
            override val name: String = name
            override val criticality: Int = criticality

            override suspend fun scan(): List<Signal> = emptyList()
        }
}
