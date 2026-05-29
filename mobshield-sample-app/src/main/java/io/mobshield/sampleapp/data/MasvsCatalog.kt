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

package io.mobshield.sampleapp.data

import io.mobshield.sampleapp.model.MasvsRowUi

object MasvsCatalog {
    val rows: List<MasvsRowUi> =
        listOf(
            MasvsRowUi("MASVS-RES-1", "Untrusted device (root/JB)", "Yes"),
            MasvsRowUi("MASVS-RES-2", "Reverse engineering / tampering", "Partial"),
            MasvsRowUi("MASVS-RES-3", "Runtime process integrity", "Partial"),
            MasvsRowUi("MASVS-RES-4", "Emulator detection", "Yes"),
            MasvsRowUi("MASVS-RES-5", "Platform API integrity", "Partial"),
            MasvsRowUi("MASVS-RES-6", "Obfuscation / anti-tamper", "No (MVP)"),
            MasvsRowUi("MASVS-RES-7", "Anti-debug", "Yes"),
            MasvsRowUi("MASVS-RES-8", "Anti-instrumentation", "Partial"),
            MasvsRowUi("MASVS-RES-9", "Device binding / attestation", "No (MVP)"),
            MasvsRowUi("MASVS-RES-10", "Anti-emulator / automation", "Yes"),
        )
}
