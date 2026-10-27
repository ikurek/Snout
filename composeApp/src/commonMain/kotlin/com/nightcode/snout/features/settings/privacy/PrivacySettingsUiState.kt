package com.nightcode.snout.features.settings.privacy

import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings

internal data class PrivacySettingsUiState(
    /** What the person lets Snout report; null until read. */
    val settings: TelemetrySettings? = null,
    /** False in builds that never report (debug builds, builds without Snout's PostHog key): no switches then. */
    val canReport: Boolean = true,
)
