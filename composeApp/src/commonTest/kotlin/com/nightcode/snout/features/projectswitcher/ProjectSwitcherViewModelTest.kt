package com.nightcode.snout.features.projectswitcher

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.telemetry.ProjectSelectionSource
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackProjectSelected
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

@OptIn(TestData::class)
class ProjectSwitcherViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val telemetryRepository = mock<TelemetryRepository>(MockMode.autoUnit)

        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>()

        fun subject(
            organizations: List<Organization> = listOf(TestOrganization),
            refreshResult: Either<DataError, Unit> = Either.Right(Unit),
        ): ProjectSwitcherViewModel {
            every { organizationRepository.observeOrganizations() } returns flowOf(organizations)
            everySuspend { organizationRepository.refreshOrganizations() } returns refreshResult
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            return ProjectSwitcherViewModel(
                observeOrganizations = ObserveOrganizations(organizationRepository),
                refreshOrganizations = RefreshOrganizations(organizationRepository),
                observeSelectedProject = ObserveSelectedProject(appStateRepository),
                saveSelectedProject = SaveSelectedProject(appStateRepository),
                trackProjectSelected = TrackProjectSelected(telemetryRepository),
            )
        }

        should("refresh the organizations on start and name the selected project") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe TestProjectSwitcherUiStateLoaded
                verifySuspend { organizationRepository.refreshOrganizations() }
            }
        }

        should("keep showing the stored organizations when the refresh fails") {
            val viewModel = subject(refreshResult = Either.Left(DataError.ConnectionError))
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe TestProjectSwitcherUiStateLoaded
            }
        }

        should("leave the current project name blank when the selected project is no longer listed") {
            val otherOrganization = TestOrganization.copy(id = Uuid.parse("00000000-0000-0000-0000-000000000002"))
            val viewModel = subject(organizations = listOf(otherOrganization))
            viewModel.uiState.subscribed {
                viewModel.uiState.value.currentProjectName shouldBe ""
            }
        }

        should("report no organizations when the list is empty") {
            val viewModel = subject(organizations = emptyList())
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe ProjectSwitcherUiState.NotFound(currentProjectName = "")
            }
        }

        should("save the selected project and report where it was picked") {
            val viewModel = subject()
            viewModel.onProjectSelected(TestOrganization.id, projectId = 7, ProjectSelectionSource.Settings).join()
            verifySuspend { appStateRepository.saveSelectedProject(TestOrganization.id, 7) }
            verify { telemetryRepository.track(TelemetryEvent.ProjectSelected(ProjectSelectionSource.Settings)) }
        }
    })
