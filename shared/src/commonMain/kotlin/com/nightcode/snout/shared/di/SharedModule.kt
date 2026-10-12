package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.data.activitylog.ActivityLogDataModule
import com.nightcode.snout.shared.data.analytics.AnalyticsDataModule
import com.nightcode.snout.shared.data.annotations.AnnotationsDataModule
import com.nightcode.snout.shared.data.appstate.AppStateDataModule
import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.errortracking.ErrorTrackingDataModule
import com.nightcode.snout.shared.data.experiments.ExperimentsDataModule
import com.nightcode.snout.shared.data.featureflags.FeatureFlagsDataModule
import com.nightcode.snout.shared.data.insights.InsightsDataModule
import com.nightcode.snout.shared.data.links.LinksDataModule
import com.nightcode.snout.shared.data.logs.LogsDataModule
import com.nightcode.snout.shared.data.organization.OrganizationDataModule
import com.nightcode.snout.shared.data.user.UserDataModule
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
        DataCommonModule::class,
        ErrorTrackingDataModule::class,
        ExperimentsDataModule::class,
        FeatureFlagsDataModule::class,
        InsightsDataModule::class,
        LinksDataModule::class,
        LogsDataModule::class,
        OrganizationDataModule::class,
        UserDataModule::class,
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
