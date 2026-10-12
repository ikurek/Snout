package com.nightcode.snout.features.experiments.detail

import arrow.core.Either
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.experiments.GetExperiment
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class ExperimentDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ExperimentRepository>()

        fun subject() = ExperimentDetailViewModel(
            experimentId = TestExperiment.id,
            getExperiment = GetExperiment(GetSelectedProjectId(appStateRepository), repository),
        )

        beforeTest { everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject }

        should("load the experiment of the selected project on start") {
            everySuspend { repository.getExperiment(TestSelectedProject.projectId, TestExperiment.id) } returns
                Either.Right(TestExperiment)
            subject().uiState.value shouldBe LoadState.Loaded(TestExperiment)
        }

        should("show the error and recover on retry") {
            everySuspend { repository.getExperiment(TestSelectedProject.projectId, TestExperiment.id) } sequentially {
                returns(Either.Left(DataError.NotFound))
                returns(Either.Right(TestExperiment))
            }
            val viewModel = subject()
            viewModel.uiState.value shouldBe LoadState.Error(DataError.NotFound)
            viewModel.refresh()
            viewModel.uiState.value shouldBe LoadState.Loaded(TestExperiment)
        }
    })
