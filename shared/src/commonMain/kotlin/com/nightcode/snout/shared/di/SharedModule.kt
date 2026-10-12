package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.domain.usecase.activitylog.ActivityLogUsecaseModule
import com.nightcode.snout.shared.domain.usecase.analytics.AnalyticsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.annotations.AnnotationsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.auth.AuthUsecaseModule
import com.nightcode.snout.shared.domain.usecase.errortracking.ErrorTrackingUsecaseModule
import com.nightcode.snout.shared.domain.usecase.experiments.ExperimentsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.featureflags.FeatureFlagsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.insights.InsightsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.links.LinksUsecaseModule
import com.nightcode.snout.shared.domain.usecase.logs.LogsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.organization.OrganizationUsecaseModule
import com.nightcode.snout.shared.domain.usecase.user.UserUsecaseModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        ActivityLogUsecaseModule::class,
        AnalyticsUsecaseModule::class,
        AnnotationsUsecaseModule::class,
        AppStateUsecaseModule::class,
        AuthUsecaseModule::class,
        ErrorTrackingUsecaseModule::class,
        ExperimentsUsecaseModule::class,
        FeatureFlagsUsecaseModule::class,
        InsightsUsecaseModule::class,
        LinksUsecaseModule::class,
        LogsUsecaseModule::class,
        OrganizationUsecaseModule::class,
        UserUsecaseModule::class,
    ],
)
class SharedModule
