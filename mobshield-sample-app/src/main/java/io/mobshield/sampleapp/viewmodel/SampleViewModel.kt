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

package io.mobshield.sampleapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.mobshield.core.MobShield
import io.mobshield.core.MobShieldState
import io.mobshield.core.RiskLevel
import io.mobshield.sampleapp.data.MasvsCatalog
import io.mobshield.sampleapp.mobshield.SampleMobShieldController
import io.mobshield.sampleapp.mobshield.TimberMobShieldListener
import io.mobshield.sampleapp.model.ModuleId
import io.mobshield.sampleapp.model.PostureUi
import io.mobshield.sampleapp.model.SamplePreferences
import io.mobshield.sampleapp.model.SignalRowUi
import io.mobshield.sampleapp.model.ThreatCardUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SampleViewModel(application: Application) : AndroidViewModel(application) {
    private val _prefs = MutableStateFlow(SamplePreferences())
    val prefs: StateFlow<SamplePreferences> = _prefs.asStateFlow()

    private val _threats = MutableStateFlow<List<ThreatCardUi>>(emptyList())
    val threats: StateFlow<List<ThreatCardUi>> = _threats.asStateFlow()

    private val _signals = MutableStateFlow<List<SignalRowUi>>(emptyList())
    val signals: StateFlow<List<SignalRowUi>> = _signals.asStateFlow()

    private val _diagnosticsLoading = MutableStateFlow(false)
    val diagnosticsLoading: StateFlow<Boolean> = _diagnosticsLoading.asStateFlow()

    private val _posture = MutableStateFlow(idlePosture())
    val posture: StateFlow<PostureUi> = _posture.asStateFlow()

    val sdkVersion: String = MobShield.getVersion()
    val buildEntropyPreview: String = SampleMobShieldController.buildEntropyPreview()
    val nativeSelfCheck: Int = MobShield.selfCheck()
    val masvsRows = MasvsCatalog.rows

    private val listener =
        TimberMobShieldListener(
            onThreatReceived = { event ->
                _threats.update { current ->
                    listOf(ThreatCardUi.from(event)) + current
                }
                refreshPosture()
            },
            onScanFinished = { events ->
                _threats.value = events.map(ThreatCardUi::from).sortedByDescending { it.score }
                refreshPosture()
            },
        )

    fun startMobShield() {
        SampleMobShieldController.start(getApplication(), _prefs.value, listener)
        refreshPosture()
    }

    fun stopMobShield() {
        SampleMobShieldController.stop()
        refreshPosture()
    }

    fun rescan() {
        SampleMobShieldController.restart(getApplication(), _prefs.value, listener)
        refreshPosture()
    }

    fun clearThreats() {
        _threats.value = emptyList()
    }

    fun setDetectOnly(enabled: Boolean) {
        _prefs.update { it.copy(detectOnly = enabled) }
        applyConfigIfRunning()
    }

    fun setAllowDeveloperSignals(enabled: Boolean) {
        _prefs.update { it.copy(allowDeveloperSignals = enabled) }
        applyConfigIfRunning()
    }

    fun toggleModule(
        module: ModuleId,
        enabled: Boolean,
    ) {
        _prefs.update { prefs ->
            val modules =
                if (enabled) {
                    prefs.enabledModules + module
                } else {
                    prefs.enabledModules - module
                }
            prefs.copy(enabledModules = modules)
        }
        applyConfigIfRunning()
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            _diagnosticsLoading.value = true
            val collected =
                SampleMobShieldController.collectSignals(getApplication(), _prefs.value)
            _signals.value =
                collected
                    .sortedByDescending { it.weight * it.confidence }
                    .map(SignalRowUi::from)
            _diagnosticsLoading.value = false
        }
    }

    private fun applyConfigIfRunning() {
        val state = MobShield.getState()
        if (state.running) {
            SampleMobShieldController.restart(getApplication(), _prefs.value, listener)
            refreshPosture()
        }
    }

    private fun refreshPosture() {
        val state: MobShieldState = MobShield.getState()
        _posture.value =
            PostureUi(
                riskLevel = state.riskLevel,
                running = state.running,
                activeThreatCount = state.activeThreats.size,
                signalSetVersion = state.signalSetVersion,
                lastScanMs = state.lastScanMs,
            )
    }

    private fun idlePosture(): PostureUi =
        PostureUi(
            riskLevel = RiskLevel.NONE,
            running = false,
            activeThreatCount = 0,
            signalSetVersion = MobShield.SIGNAL_SET_VERSION,
            lastScanMs = 0L,
        )
}
