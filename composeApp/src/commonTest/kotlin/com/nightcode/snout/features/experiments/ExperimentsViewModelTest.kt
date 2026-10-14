package com.nightcode.snout.features.experiments

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.experiments.GetExperiments
import com.nightcode.snout.shared.domain.usecase.experiments.ObserveExperimentReloads
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(TestData::class)
class ExperimentsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ExperimentRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val loaded = Either.Right(listOf(TestExperiment, TestExperiment.copy(id = 9, archived = true)))

        fun subject(): ExperimentsViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            return ExperimentsViewModel(
                getExperiments = GetExperiments(GetSelectedProjectId(appStateRepository), repository),
                observeExperimentReloads = ObserveExperimentReloads(ObserveSelectedProjectId(appStateRepository)),
            )
        }

        should("load the experiments on start and hide archived ones") {
            everySuspend { repository.getExperiments(any()) } returns loaded
            val viewModel = subject()
            viewModel.uiState.value shouldBe ExperimentsUiState(experiments = listOf(TestExperiment))
            verifySuspend { repository.getExperiments(TestSelectedProject.projectId) }
        }

        should("keep the experiments and report a failed pull to refresh") {
            everySuspend { repository.getExperiments(any()) } sequentially {
                returns(loaded)
                returns(Either.Left(DataError.RateLimited))
            }
            val viewModel = subject()
            viewModel.refresh()
            viewModel.uiState.value shouldBe ExperimentsUiState(
                experiments = listOf(TestExperiment),
                error = DataError.RateLimited,
            )
        }

        should("load again when another project is selected") {
            everySuspend { repository.getExperiments(any()) } returns loaded
            subject()
            selected.value = TestSelectedProject.copy(projectId = 2)
            verifySuspend { repository.getExperiments(2) }
        }
    })
