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

package io.mobshield.sampleapp.mobshield

import io.mobshield.core.MobShieldListener
import io.mobshield.core.ThreatEvent
import timber.log.Timber

/** Structured MobShield callbacks routed to Timber. */
class TimberMobShieldListener(
    private val onThreatReceived: (ThreatEvent) -> Unit,
    private val onScanFinished: (List<ThreatEvent>) -> Unit,
) : MobShieldListener {
    override fun onThreat(event: ThreatEvent) {
        Timber.i(
            "MobShield threat type=%s severity=%s score=%d signals=%s",
            event.type,
            event.severity,
            event.score,
            event.signals,
        )
        onThreatReceived(event)
    }

    override fun onAllChecksFinished(events: List<ThreatEvent>) {
        Timber.i("MobShield scan finished events=%d", events.size)
        onScanFinished(events)
    }
}
