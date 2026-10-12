package com.nightcode.snout.features.onboarding.project

import arrow.core.Either
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.organization.TestProject
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ProjectSelectionViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit) {
            every { observeOrganizations() } returns flowOf(listOf(TestOrganization))
        }
        val cacheRepository = mock<CacheRepository>(autoUnit)
        val authRepository = mock<AuthRepository>(autoUnit)

        fun subject() = ProjectSelectionViewModel(
            observeOrganizations = ObserveOrganizations(organizationRepository),
            refreshOrganizations = RefreshOrganizations(organizationRepository),
            saveSelectedProject = SaveSelectedProject(appStateRepository),
            logout = Logout(
                authRepository,
                ClearLocalData(appStateRepository, organizationRepository, cacheRepository),
            ),
        )

        should("wait for the first refresh before listing the organizations") {
            everySuspend { organizationRepository.refreshOrganizations() } returns Either.Right(Unit)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe LoadState.Loading
                viewModel.refresh()
                viewModel.uiState.value shouldBe TestProjectSelectionUiStateLoaded
            }
        }

        should("show the refresh error and recover on retry") {
            everySuspend { organizationRepository.refreshOrganizations() } sequentially {
                returns(Either.Left(DataError.Unauthorized))
                returns(Either.Right(Unit))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.refresh()
                viewModel.uiState.value shouldBe LoadState.Error(DataError.Unauthorized)
                viewModel.refresh()
                viewModel.uiState.value shouldBe TestProjectSelectionUiStateLoaded
            }
        }

        should("save the chosen project and move on") {
            val viewModel = subject()

            viewModel.onProjectSelected(TestOrganization.id, TestProject.id)

            viewModel.sideEffects.first() shouldBe ProjectSelectionSideEffect.ProjectSelected
            verifySuspend { appStateRepository.saveSelectedProject(TestOrganization.id, TestProject.id) }
        }

        should("log out and clear local data when disconnecting") {
            subject().disconnect().join()
            verifySuspend {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                cacheRepository.clear()
                authRepository.clear()
            }
        }
    })
