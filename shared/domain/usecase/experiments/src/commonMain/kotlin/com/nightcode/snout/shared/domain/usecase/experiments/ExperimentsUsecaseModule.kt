package com.nightcode.snout.shared.domain.usecase.experiments

import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ExperimentsUsecaseModule {
    @Single
    fun provideObserveExperiments(observe: ObserveSelectedProjectId, repository: ExperimentRepository) =
        ObserveExperiments(observe, repository)

    @Single
    fun provideRefreshExperiments(get: GetSelectedProjectId, repository: ExperimentRepository) =
        RefreshExperiments(get, repository)

    @Single
    fun provideGetExperiment(get: GetSelectedProjectId, repository: ExperimentRepository) =
        GetExperiment(get, repository)
}
