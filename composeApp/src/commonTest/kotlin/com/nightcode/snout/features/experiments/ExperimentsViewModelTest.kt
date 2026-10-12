package com.nightcode.snout.features.experiments

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.experiments.ObserveExperiments
import com.nightcode.snout.shared.domain.usecase.experiments.RefreshExperiments
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ExperimentsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ExperimentRepository>()

        should("hide archived experiments") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeExperiments(any()) } returns
                flowOf(listOf(TestExperiment, TestExperiment.copy(id = 9, archived = true)))
            everySuspend { repository.refreshExperiments(any()) } returns Either.Right(Unit)
            val viewModel = ExperimentsViewModel(
                refreshExperiments = RefreshExperiments(GetSelectedProjectId(appStateRepository), repository),
                observeExperiments = ObserveExperiments(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Content(listOf(TestExperiment), isRefreshing = false)
            }
        }
    })
