package com.nightcode.snout.shared.domain.usecase.experiments

import com.nightcode.snout.shared.data.experiments.ExperimentsDataModule
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ExperimentsDataModule::class, AppStateUsecaseModule::class])
class ExperimentsUsecaseModule {
    @Single
    fun observeExperiments(observe: ObserveSelectedProjectId, repository: ExperimentRepository) =
        ObserveExperiments(observe, repository)

    @Single
    fun refreshExperiments(get: GetSelectedProjectId, repository: ExperimentRepository) =
        RefreshExperiments(get, repository)

    @Single
    fun getExperiment(get: GetSelectedProjectId, repository: ExperimentRepository) = GetExperiment(get, repository)
}
