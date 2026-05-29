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

/** Normalized threat category aligned with MOBSHIELD_SPEC section C.2. */
enum class ThreatType {
    PRIVILEGED_ACCESS,
    HOOK_FRAMEWORK,
    DEBUGGER,
    EMULATOR,
    AUTOMATION,
    APP_INTEGRITY,
    DEVELOPER_MODE,
    ADB_ENABLED,
    UNOFFICIAL_STORE,
}
