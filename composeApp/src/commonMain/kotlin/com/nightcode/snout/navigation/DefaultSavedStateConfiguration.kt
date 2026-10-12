package com.nightcode.snout.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.nightcode.snout.features.dashboards.detail.DashboardDetailNavKey
import com.nightcode.snout.features.errortracking.detail.ErrorTrackingIssueNavKey
import com.nightcode.snout.features.experiments.detail.ExperimentDetailNavKey
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailNavKey
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.home.SectionNavKey
import com.nightcode.snout.features.insights.detail.InsightDetailNavKey
import com.nightcode.snout.features.logs.entry.LogEntryNavKey
import com.nightcode.snout.features.logs.service.LogServiceNavKey
import com.nightcode.snout.features.onboarding.OnboardingNavKey
import com.nightcode.snout.features.settings.SettingsNavKey
import com.nightcode.snout.features.settings.analytics.AnalyticsSettingsNavKey
import com.nightcode.snout.features.settings.navigation.NavigationSettingsNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val DefaultSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeNavKey::class, HomeNavKey.serializer())
            subclass(OnboardingNavKey::class, OnboardingNavKey.serializer())
            subclass(SettingsNavKey::class, SettingsNavKey.serializer())
            subclass(NavigationSettingsNavKey::class, NavigationSettingsNavKey.serializer())
            subclass(AnalyticsSettingsNavKey::class, AnalyticsSettingsNavKey.serializer())
            subclass(SectionNavKey::class, SectionNavKey.serializer())
            subclass(DashboardDetailNavKey::class, DashboardDetailNavKey.serializer())
            subclass(InsightDetailNavKey::class, InsightDetailNavKey.serializer())
            subclass(FeatureFlagDetailNavKey::class, FeatureFlagDetailNavKey.serializer())
            subclass(ErrorTrackingIssueNavKey::class, ErrorTrackingIssueNavKey.serializer())
            subclass(LogServiceNavKey::class, LogServiceNavKey.serializer())
            subclass(LogEntryNavKey::class, LogEntryNavKey.serializer())
            subclass(ExperimentDetailNavKey::class, ExperimentDetailNavKey.serializer())
        }
    }
}
