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

package io.mobshield.gradle

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class MobShieldExtension @Inject constructor(objects: ObjectFactory) {
    var expectedSigningCertSha256: String = ""
    var expectedPackageName: String = ""
    var expectedInstallers: List<String> = listOf("com.android.vending")
    var aggressive: Boolean = false
    /** Optional 64-char hex seed. Generated per build when null. */
    var randomSeed: String? = null
}
