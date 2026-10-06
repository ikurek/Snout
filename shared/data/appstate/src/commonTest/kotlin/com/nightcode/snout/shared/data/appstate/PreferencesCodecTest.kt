package com.nightcode.snout.shared.data.appstate

import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.storage.datastore.StoredAnalyticsPreferences
import com.nightcode.snout.shared.storage.datastore.StoredNavigationPreferences
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class PreferencesCodecTest :
    ShouldSpec({
        should("fall back to default navigation when nothing is stored") {
            PreferencesCodec.decodeNavigation(StoredNavigationPreferences(null, null)) shouldBe
                NavigationPreferences.Default
        }

        should("decode stored tabs and landing section") {
            val stored = StoredNavigationPreferences(
                tabs = "Experiments,Dashboards,Annotations,FeatureFlags",
                landing = "Dashboards",
            )
            PreferencesCodec.decodeNavigation(stored) shouldBe NavigationPreferences(
                tabs = listOf(
                    HomeSection.Experiments,
                    HomeSection.Dashboards,
                    HomeSection.Annotations,
                    HomeSection.FeatureFlags,
                ),
                landing = HomeSection.Dashboards,
            )
        }

        should("fall back to default navigation when stored tabs are unknown, duplicated or too few") {
            PreferencesCodec.decodeNavigation(
                StoredNavigationPreferences("Analytics,Nope,Insights,Flags", null),
            ) shouldBe
                NavigationPreferences.Default
            PreferencesCodec.decodeNavigation(
                StoredNavigationPreferences("Analytics,Analytics,Insights,FeatureFlags", "Analytics"),
            ) shouldBe NavigationPreferences.Default
        }

        should("round-trip navigation tabs") {
            val preferences = NavigationPreferences.Default.withTab(0, HomeSection.ActivityLog)
            val stored = StoredNavigationPreferences(PreferencesCodec.encodeTabs(preferences), preferences.landing.name)
            PreferencesCodec.decodeNavigation(stored) shouldBe preferences
        }

        should("fall back to default analytics preferences when nothing is stored") {
            PreferencesCodec.decodeAnalytics(StoredAnalyticsPreferences(null, null, null, null)) shouldBe
                AnalyticsPreferences.Default
        }

        should("decode analytics preferences and ignore unknown names") {
            val stored = StoredAnalyticsPreferences(
                defaultRange = "Last30Days",
                metrics = "Visitors,Other,Mystery",
                showVisitorsTrend = false,
                breakdowns = "Page,Country",
            )
            PreferencesCodec.decodeAnalytics(stored) shouldBe AnalyticsPreferences(
                defaultRange = AnalyticsRange.Last30Days,
                metrics = setOf(OverviewMetricKey.Visitors),
                showVisitorsTrend = false,
                breakdowns = setOf(WebBreakdown.Page, WebBreakdown.Country),
            )
        }

        should("keep all metrics when the stored list is empty") {
            val stored = StoredAnalyticsPreferences(null, "", null, "")
            val decoded = PreferencesCodec.decodeAnalytics(stored)
            decoded.metrics shouldBe AnalyticsPreferences.Default.metrics
            decoded.breakdowns shouldBe emptySet()
        }
    })
