package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
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

class NavigationPreferencesUseCasesTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>(autoUnit)

        should("observe repository navigation preferences") {
            val preferences = NavigationPreferences.Default.withTab(1, HomeSection.Experiments)
            every { repository.observeNavigationPreferences() } returns flowOf(preferences)
            ObserveNavigationPreferences(repository)().first() shouldBe preferences
        }

        should("save valid navigation preferences") {
            val preferences = NavigationPreferences.Default.withLanding(HomeSection.Insights)
            SaveNavigationPreferences(repository)(preferences)
            verifySuspend { repository.saveNavigationPreferences(preferences) }
        }

        should("refuse invalid navigation preferences") {
            val invalid = NavigationPreferences(tabs = listOf(HomeSection.Analytics), landing = HomeSection.Analytics)
            shouldThrow<IllegalArgumentException> { SaveNavigationPreferences(repository)(invalid) }
        }

        should("swap sections when a tab moves into an occupied slot") {
            val preferences = NavigationPreferences.Default.withTab(0, HomeSection.Insights)
            preferences.tabs shouldBe listOf(
                HomeSection.Insights,
                HomeSection.Dashboards,
                HomeSection.Analytics,
                HomeSection.FeatureFlags,
            )
            preferences.landing shouldBe HomeSection.Analytics
        }

        should("move the landing section when it leaves the bar") {
            val preferences = NavigationPreferences.Default.withTab(0, HomeSection.Annotations)
            preferences.landing shouldBe HomeSection.Annotations
            preferences.hidden shouldBe listOf(
                HomeSection.Analytics,
                HomeSection.ErrorTracking,
                HomeSection.Experiments,
                HomeSection.ActivityLog,
            )
        }
    })
