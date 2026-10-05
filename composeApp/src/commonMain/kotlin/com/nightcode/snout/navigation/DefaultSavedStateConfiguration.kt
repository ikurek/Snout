package com.nightcode.snout.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.nightcode.snout.features.activitylog.ActivityLogNavKey
import com.nightcode.snout.features.annotations.AnnotationsNavKey
import com.nightcode.snout.features.dashboards.detail.DashboardDetailNavKey
import com.nightcode.snout.features.experiments.ExperimentsNavKey
import com.nightcode.snout.features.experiments.detail.ExperimentDetailNavKey
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailNavKey
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.insights.detail.InsightDetailNavKey
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.features.settings.SettingsNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val DefaultSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeNavKey::class, HomeNavKey.serializer())
            subclass(LoginNavKey::class, LoginNavKey.serializer())
            subclass(InitialProjectNavKey::class, InitialProjectNavKey.serializer())
            subclass(SettingsNavKey::class, SettingsNavKey.serializer())
            subclass(DashboardDetailNavKey::class, DashboardDetailNavKey.serializer())
            subclass(InsightDetailNavKey::class, InsightDetailNavKey.serializer())
            subclass(FeatureFlagDetailNavKey::class, FeatureFlagDetailNavKey.serializer())
            subclass(ExperimentsNavKey::class, ExperimentsNavKey.serializer())
            subclass(ExperimentDetailNavKey::class, ExperimentDetailNavKey.serializer())
            subclass(AnnotationsNavKey::class, AnnotationsNavKey.serializer())
            subclass(ActivityLogNavKey::class, ActivityLogNavKey.serializer())
        }
    }
}
