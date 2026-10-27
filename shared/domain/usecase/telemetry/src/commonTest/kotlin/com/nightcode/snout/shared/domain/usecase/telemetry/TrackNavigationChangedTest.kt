package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.core.spec.style.ShouldSpec

class TrackNavigationChangedTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("report the tabs and the landing tab chosen") {
            val preferences = NavigationPreferences.Default.withLanding(HomeSection.FeatureFlags)

            TrackNavigationChanged(repository)(preferences)

            verify { repository.track(TelemetryEvent.NavigationChanged(preferences.tabs, HomeSection.FeatureFlags)) }
        }
    })
