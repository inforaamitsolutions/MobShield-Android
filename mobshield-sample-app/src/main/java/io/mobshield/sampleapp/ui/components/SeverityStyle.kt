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

package io.mobshield.sampleapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.mobshield.core.Severity

@Composable
fun severityColor(severity: Severity): Color =
    when (severity) {
        Severity.INFO -> MaterialTheme.colorScheme.outline
        Severity.LOW -> MaterialTheme.colorScheme.primary
        Severity.MEDIUM -> Color(0xFFF9A825)
        Severity.HIGH -> Color(0xFFEF6C00)
        Severity.CRITICAL -> MaterialTheme.colorScheme.error
    }
