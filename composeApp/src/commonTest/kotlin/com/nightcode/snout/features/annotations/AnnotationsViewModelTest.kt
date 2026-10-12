package com.nightcode.snout.features.annotations

import arrow.core.Either
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.annotations.TestAnnotation
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.annotations.CreateAnnotation
import com.nightcode.snout.shared.domain.usecase.annotations.GetAnnotations
import com.nightcode.snout.shared.domain.usecase.annotations.ObserveAnnotationReloads
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.testing.subscribed
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(TestData::class)
class AnnotationsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnnotationRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val changes = MutableSharedFlow<Unit>()
        val created = TestAnnotation.copy(id = 2, content = "Release")

        fun subject(): AnnotationsViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.changes } returns changes
            val projectId = GetSelectedProjectId(appStateRepository)
            return AnnotationsViewModel(
                createAnnotation = CreateAnnotation(projectId, repository),
                getAnnotations = GetAnnotations(projectId, repository),
                observeAnnotationReloads = ObserveAnnotationReloads(
                    ObserveSelectedProjectId(appStateRepository),
                    repository,
                ),
            )
        }

        should("load the annotations on start") {
            everySuspend { repository.getAnnotations(any()) } returns Either.Right(listOf(TestAnnotation))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.annotations shouldBe
                    RefreshableUiState.Content(listOf(TestAnnotation), isRefreshing = false)
                verifySuspend { repository.getAnnotations(TestSelectedProject.projectId) }
            }
        }

        should("keep the annotations and report a failed pull to refresh") {
            everySuspend { repository.getAnnotations(any()) } sequentially {
                returns(Either.Right(listOf(TestAnnotation)))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.refresh()
                viewModel.uiState.value.annotations shouldBe RefreshableUiState.Content(
                    listOf(TestAnnotation),
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("load again after an annotation was created in the app") {
            everySuspend { repository.getAnnotations(any()) } sequentially {
                returns(Either.Right(listOf(TestAnnotation)))
                returns(Either.Right(listOf(created, TestAnnotation)))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                changes.emit(Unit)
                viewModel.uiState.value.annotations shouldBe
                    RefreshableUiState.Content(listOf(created, TestAnnotation), isRefreshing = false)
            }
        }

        should("load again when another project is selected") {
            everySuspend { repository.getAnnotations(any()) } returns Either.Right(listOf(TestAnnotation))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.getAnnotations(2) }
            }
        }

        should("emit Created and reset status after a successful create") {
            everySuspend { repository.getAnnotations(any()) } returns Either.Right(listOf(TestAnnotation))
            everySuspend { repository.createAnnotation(any(), "Release", any()) } returns Either.Right(created)
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
            everySuspend { repository.getAnnotations(any()) } returns Either.Right(listOf(TestAnnotation))
            everySuspend { repository.createAnnotation(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.create("Release", TestAnnotation.dateMarker)
                viewModel.uiState.value.create shouldBe CreateStatus.Failed(DataError.Forbidden)
            }
        }
    })
