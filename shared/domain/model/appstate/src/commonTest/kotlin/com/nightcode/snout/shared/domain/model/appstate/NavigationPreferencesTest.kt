package com.nightcode.snout.shared.domain.model.appstate

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class NavigationPreferencesTest :
    ShouldSpec({
        val default = NavigationPreferences.Default

        should("put a hidden section into the slot and move the replaced one under more") {
            val updated = default.withTab(1, HomeSection.Logs)
            updated.tabs shouldBe listOf(
                HomeSection.Analytics,
                HomeSection.Logs,
                HomeSection.Insights,
                HomeSection.FeatureFlags,
            )
            (HomeSection.Dashboards in updated.hidden).shouldBeTrue()
            (HomeSection.Logs in updated.hidden).shouldBeFalse()
            updated.landing shouldBe HomeSection.Analytics
            updated.isValid.shouldBeTrue()
        }

        should("swap two tabs when the section already sits in another slot") {
            default.withTab(0, HomeSection.FeatureFlags).tabs shouldBe listOf(
                HomeSection.FeatureFlags,
                HomeSection.Dashboards,
                HomeSection.Insights,
                HomeSection.Analytics,
            )
        }

        should("keep the tabs when the section is put into its own slot") {
            default.withTab(2, HomeSection.Insights) shouldBe default
        }

        should("keep the landing section when it is swapped into another slot") {
            default.withTab(3, HomeSection.Analytics).landing shouldBe HomeSection.Analytics
        }

        should("land on the first tab when the landing section is replaced") {
            val updated = default.withTab(0, HomeSection.Experiments)
            updated.landing shouldBe HomeSection.Experiments
            updated.isValid.shouldBeTrue()
        }

        should("refuse a slot outside the tabs") {
            shouldThrow<IllegalArgumentException> { default.withTab(-1, HomeSection.Logs) }
            shouldThrow<IllegalArgumentException> { default.withTab(NavigationPreferences.TabCount, HomeSection.Logs) }
        }

        should("land on one of the tabs") {
            default.withLanding(HomeSection.Insights).landing shouldBe HomeSection.Insights
        }

        should("fall back to the first tab when landing on a section under more") {
            default.withLanding(HomeSection.ActivityLog).landing shouldBe HomeSection.Analytics
        }

        should("list every section not in the tabs as hidden") {
            default.hidden shouldBe listOf(
                HomeSection.ErrorTracking,
                HomeSection.Logs,
                HomeSection.Experiments,
                HomeSection.Annotations,
                HomeSection.ActivityLog,
            )
        }

        should("be invalid with duplicate tabs, the wrong tab count or a landing outside the tabs") {
            default.isValid.shouldBeTrue()
            default.copy(tabs = List(NavigationPreferences.TabCount) { HomeSection.Analytics }).isValid.shouldBeFalse()
            default.copy(tabs = default.tabs.take(3)).isValid.shouldBeFalse()
            default.copy(landing = HomeSection.Logs).isValid.shouldBeFalse()
        }
    })
