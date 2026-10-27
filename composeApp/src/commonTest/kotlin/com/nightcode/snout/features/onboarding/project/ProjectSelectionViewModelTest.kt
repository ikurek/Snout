package com.nightcode.snout.features.onboarding.project

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.organization.TestProject
import com.nightcode.snout.shared.domain.model.telemetry.InstanceKind
import com.nightcode.snout.shared.domain.model.telemetry.ProjectSelectionSource
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackOnboarded
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackProjectSelected
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ProjectSelectionViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val telemetryRepository = mock<TelemetryRepository>(MockMode.autoUnit)

        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit) {
            every { observeOrganizations() } returns flowOf(listOf(TestOrganization))
        }
        val userRepository = mock<UserRepository>(autoUnit)
        val authRepository = mock<AuthRepository>(autoUnit)

        fun subject() = ProjectSelectionViewModel(
            observeOrganizations = ObserveOrganizations(organizationRepository),
            refreshOrganizations = RefreshOrganizations(organizationRepository),
            saveSelectedProject = SaveSelectedProject(appStateRepository),
            trackOnboarded = TrackOnboarded(GetInstanceUrl(authRepository), telemetryRepository),
            trackProjectSelected = TrackProjectSelected(telemetryRepository),
            logout = Logout(
                authRepository,
                ClearLocalData(appStateRepository, organizationRepository, userRepository),
            ),
        )

        should("wait for the first refresh before listing the organizations") {
            everySuspend { organizationRepository.refreshOrganizations() } returns Either.Right(Unit)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe ProjectSelectionUiState()
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
                viewModel.uiState.value shouldBe ProjectSelectionUiState(error = DataError.Unauthorized)
                viewModel.refresh()
                viewModel.uiState.value shouldBe TestProjectSelectionUiStateLoaded
            }
        }

        should("save the chosen project, report onboarding by the instance's kind and move on") {
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            val viewModel = subject()

            viewModel.onProjectSelected(TestOrganization.id, TestProject.id)

            viewModel.sideEffects.first() shouldBe ProjectSelectionSideEffect.ProjectSelected
            verifySuspend { appStateRepository.saveSelectedProject(TestOrganization.id, TestProject.id) }
            verify { telemetryRepository.track(TelemetryEvent.ProjectSelected(ProjectSelectionSource.Onboarding)) }
            verify { telemetryRepository.track(TelemetryEvent.Onboarded(InstanceKind.CloudEu)) }
        }

        should("log out and clear local data when disconnecting") {
            subject().disconnect().join()
            verifySuspend {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                userRepository.clear()
                authRepository.clear()
            }
        }
    })
