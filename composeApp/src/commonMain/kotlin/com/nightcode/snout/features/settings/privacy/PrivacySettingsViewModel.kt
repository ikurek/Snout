package com.nightcode.snout.features.settings.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.telemetry.IsTelemetryAvailable
import com.nightcode.snout.shared.domain.usecase.telemetry.ObserveTelemetrySettings
import com.nightcode.snout.shared.domain.usecase.telemetry.SaveTelemetrySettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/** Where the privacy policy is published: with the source, so its history is public too. */
internal const val PrivacyPolicyUrl = "https://github.com/ikurek/snout/blob/master/PRIVACY.md"

@KoinViewModel
internal class PrivacySettingsViewModel(
    private val saveTelemetrySettings: SaveTelemetrySettings,
    private val openInBrowser: OpenInBrowser,
    observeTelemetrySettings: ObserveTelemetrySettings,
    isTelemetryAvailable: IsTelemetryAvailable,
) : ViewModel() {

    private val canReport = isTelemetryAvailable()

    val uiState: StateFlow<PrivacySettingsUiState> = observeTelemetrySettings()
        .map { PrivacySettingsUiState(settings = it, canReport = canReport) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrivacySettingsUiState(canReport = canReport))

    fun onUsageToggled(enabled: Boolean) = update { it.copy(usage = enabled) }

    fun onErrorsToggled(enabled: Boolean) = update { it.copy(errors = enabled) }

    fun onPrivacyPolicyClicked() = openInBrowser(PrivacyPolicyUrl)

    private fun update(transform: (TelemetrySettings) -> TelemetrySettings) {
        val current = uiState.value.settings ?: return
        viewModelScope.launch { saveTelemetrySettings(transform(current)) }
    }
}
