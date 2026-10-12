package com.nightcode.snout.features.annotations

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.annotations.TestAnnotation
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.annotations.CreateAnnotation
import com.nightcode.snout.shared.domain.usecase.annotations.ObserveAnnotations
import com.nightcode.snout.shared.domain.usecase.annotations.RefreshAnnotations
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(TestData::class)
class AnnotationsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnnotationRepository>()

        fun subject(): AnnotationsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeAnnotations(any()) } returns flowOf(listOf(TestAnnotation))
            everySuspend { repository.refreshAnnotations(any()) } returns Either.Right(Unit)
            val projectId = GetSelectedProjectId(appStateRepository)
            return AnnotationsViewModel(
                refreshAnnotations = RefreshAnnotations(projectId, repository),
                createAnnotation = CreateAnnotation(projectId, repository),
                observeAnnotations = ObserveAnnotations(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
        }

        should("list cached annotations") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.annotations shouldBe CachedUiState.Content(
                    listOf(TestAnnotation),
                    isRefreshing = false,
                )
            }
        }

        should("emit Created and reset status after a successful create") {
            everySuspend { repository.createAnnotation(any(), "Release", any()) } returns Either.Right(TestAnnotation)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                val effects = launch(
                    Dispatchers.Main,
                ) { viewModel.sideEffects.first() shouldBe AnnotationsSideEffect.Created }
                viewModel.create("Release", TestAnnotation.dateMarker)
                effects.join()
                viewModel.uiState.value.create shouldBe CreateStatus.Idle
            }
        }

        should("expose failure when creation is forbidden") {
            everySuspend { repository.createAnnotation(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.create("Release", TestAnnotation.dateMarker)
                viewModel.uiState.value.create shouldBe CreateStatus.Failed(DataError.Forbidden)
            }
        }
    })
