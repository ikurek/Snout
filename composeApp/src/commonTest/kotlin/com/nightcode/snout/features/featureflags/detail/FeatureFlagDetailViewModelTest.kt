package com.nightcode.snout.features.featureflags.detail

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.featureflags.GetFeatureFlag
import com.nightcode.snout.shared.domain.usecase.featureflags.SetFeatureFlagActive
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class FeatureFlagDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<FeatureFlagRepository>()

        fun subject(): FeatureFlagDetailViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getFeatureFlag(any(), TestFeatureFlag.id) } returns Either.Right(TestFeatureFlag)
            val projectId = GetSelectedProjectId(appStateRepository)
            return FeatureFlagDetailViewModel(
                flagId = TestFeatureFlag.id,
                getFeatureFlag = GetFeatureFlag(projectId, repository),
                setFeatureFlagActive = SetFeatureFlagActive(projectId, repository),
            )
        }

        should("load the flag on start") {
            subject().uiState.value.flag shouldBe CachedUiState.Content(TestFeatureFlag, isRefreshing = false)
        }

        should("replace the flag after a successful toggle") {
            val disabled = TestFeatureFlag.copy(active = false)
            everySuspend { repository.setActive(any(), TestFeatureFlag.id, false) } returns Either.Right(disabled)
            val viewModel = subject()
            viewModel.onToggleConfirmed(false)
            viewModel.uiState.value.flag shouldBe CachedUiState.Content(disabled, isRefreshing = false)
            viewModel.uiState.value.toggle shouldBe ToggleStatus.Idle
        }

        should("keep the flag and report forbidden when the key lacks write scope") {
            everySuspend {
                repository.setActive(
                    any(),
                    TestFeatureFlag.id,
                    false,
                )
            } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()
            viewModel.onToggleConfirmed(false)
            viewModel.uiState.value.flag shouldBe CachedUiState.Content(TestFeatureFlag, isRefreshing = false)
            viewModel.uiState.value.toggle shouldBe ToggleStatus.Failed(DataError.Forbidden)
            viewModel.dismissToggleError()
            viewModel.uiState.value.toggle shouldBe ToggleStatus.Idle
        }
    })
