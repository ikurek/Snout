package com.nightcode.snout.features.experiments

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
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
import com.nightcode.snout.shared.domain.usecase.experiments.ObserveExperiments
import com.nightcode.snout.shared.domain.usecase.experiments.RefreshExperiments
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ExperimentsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ExperimentRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(refreshResult: Either<DataError, Unit> = Either.Right(Unit)): ExperimentsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.observeExperiments(any()) } returns
                flowOf(listOf(TestExperiment, TestExperiment.copy(id = 9, archived = true)))
            everySuspend { repository.refreshExperiments(any()) } returns refreshResult
            return ExperimentsViewModel(
                refreshExperiments = RefreshExperiments(GetSelectedProjectId(appStateRepository), repository),
                observeExperiments = ObserveExperiments(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
        }

        should("hide archived experiments") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Content(listOf(TestExperiment), isRefreshing = false)
            }
        }

        should("keep the cached experiments and report a failed refresh") {
            val viewModel = subject(refreshResult = Either.Left(DataError.RateLimited))
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Content(
                    listOf(TestExperiment),
                    isRefreshing = false,
                    refreshError = DataError.RateLimited,
                )
            }
        }

        should("refresh again when another project is selected") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.refreshExperiments(2) }
            }
        }
    })
