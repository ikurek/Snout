package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.data.activitylog.ActivityLogDataModule
import com.nightcode.snout.shared.data.analytics.AnalyticsDataModule
import com.nightcode.snout.shared.data.annotations.AnnotationsDataModule
import com.nightcode.snout.shared.data.appstate.AppStateDataModule
import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import com.nightcode.snout.shared.data.errortracking.ErrorTrackingDataModule
import com.nightcode.snout.shared.data.experiments.ExperimentsDataModule
import com.nightcode.snout.shared.data.featureflags.FeatureFlagsDataModule
import com.nightcode.snout.shared.data.insights.InsightsDataModule
import com.nightcode.snout.shared.data.links.LinksDataModule
import com.nightcode.snout.shared.data.logs.LogsDataModule
import com.nightcode.snout.shared.data.organization.OrganizationDataModule
import com.nightcode.snout.shared.data.user.UserDataModule
import com.nightcode.snout.shared.domain.usecase.activitylog.ActivityLogUseCaseModule
import com.nightcode.snout.shared.domain.usecase.analytics.AnalyticsUseCaseModule
import com.nightcode.snout.shared.domain.usecase.annotations.AnnotationsUseCaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUseCaseModule
import com.nightcode.snout.shared.domain.usecase.auth.AuthUseCaseModule
import com.nightcode.snout.shared.domain.usecase.errortracking.ErrorTrackingUseCaseModule
import com.nightcode.snout.shared.domain.usecase.experiments.ExperimentsUseCaseModule
import com.nightcode.snout.shared.domain.usecase.featureflags.FeatureFlagsUseCaseModule
import com.nightcode.snout.shared.domain.usecase.insights.InsightsUseCaseModule
import com.nightcode.snout.shared.domain.usecase.links.LinksUseCaseModule
import com.nightcode.snout.shared.domain.usecase.logs.LogsUseCaseModule
import com.nightcode.snout.shared.domain.usecase.organization.OrganizationUseCaseModule
import com.nightcode.snout.shared.domain.usecase.user.UserUseCaseModule
import org.koin.core.annotation.Module

/**
 * Composition root of the shared layer: wires every repository implementation to the use cases.
 *
 * Use case modules only know repository interfaces, so the data modules are included here instead.
 */
@Module(
    includes = [
        ActivityLogDataModule::class,
        AnalyticsDataModule::class,
        AnnotationsDataModule::class,
        AppStateDataModule::class,
        AuthDataModule::class,
        CommonDataModule::class,
        ErrorTrackingDataModule::class,
        ExperimentsDataModule::class,
        FeatureFlagsDataModule::class,
        InsightsDataModule::class,
        LinksDataModule::class,
        LogsDataModule::class,
        OrganizationDataModule::class,
        UserDataModule::class,
        ActivityLogUseCaseModule::class,
        AnalyticsUseCaseModule::class,
        AnnotationsUseCaseModule::class,
        AppStateUseCaseModule::class,
        AuthUseCaseModule::class,
        ErrorTrackingUseCaseModule::class,
        ExperimentsUseCaseModule::class,
        FeatureFlagsUseCaseModule::class,
        InsightsUseCaseModule::class,
        LinksUseCaseModule::class,
        LogsUseCaseModule::class,
        OrganizationUseCaseModule::class,
        UserUseCaseModule::class,
    ],
)
class SharedModule
