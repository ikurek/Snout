package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class AnalyticsPreferencesUseCasesTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>(autoUnit)

        should("observe repository analytics preferences") {
            val preferences = AnalyticsPreferences.Default.copy(defaultRange = AnalyticsRange.Today)
            every { repository.observeAnalyticsPreferences() } returns flowOf(preferences)
            ObserveAnalyticsPreferences(repository)().first() shouldBe preferences
        }

        should("save analytics preferences") {
            val preferences = AnalyticsPreferences.Default.withMetric(OverviewMetricKey.BounceRate, enabled = false)
            SaveAnalyticsPreferences(repository)(preferences)
            verifySuspend { repository.saveAnalyticsPreferences(preferences) }
        }

        should("refuse preferences that hide every stat card") {
            val none = AnalyticsPreferences.Default.copy(metrics = emptySet())
            shouldThrow<IllegalArgumentException> { SaveAnalyticsPreferences(repository)(none) }
        }
    })
