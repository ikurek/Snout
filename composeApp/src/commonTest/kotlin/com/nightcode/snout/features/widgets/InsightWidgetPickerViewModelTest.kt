package com.nightcode.snout.features.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.organization.TestProject
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import com.nightcode.snout.widgets.ProjectChoice
import com.nightcode.snout.widgets.WidgetProjects
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

@OptIn(TestData::class)
class InsightWidgetPickerViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val authRepository = mock<AuthRepository>()
        val appStateRepository = mock<AppStateRepository>()
        val organizationRepository = mock<OrganizationRepository>()
        val repository = mock<InsightRepository>()
        val searched = TestInsightSummary.copy(id = 42, name = "Searched")
        val otherProject = 7L

        fun subject(
            key: PersonalApiKey? = TestPersonalApiKey,
            project: SelectedProject? = TestSelectedProject,
            widgetProject: Long? = null,
            projectsFail: Boolean = false,
        ) = run {
            everySuspend { authRepository.getPersonalApiKey() } returns key
            everySuspend { authRepository.getInstanceUrl() } returns "https://us.posthog.com"
            everySuspend { appStateRepository.getSelectedProject() } returns project
            everySuspend { organizationRepository.refreshOrganizations() } returns
                if (projectsFail) Either.Left(DataError.ConnectionError) else Either.Right(Unit)
            every { organizationRepository.observeOrganizations() } returns
                flowOf(if (projectsFail) emptyList() else listOf(TestOrganization))
            val isSignedIn = IsSignedIn(authRepository)
            val projectId = GetSelectedProjectId(appStateRepository)
            InsightWidgetPickerViewModel(
                widget = WidgetProject(widgetProject),
                isSignedIn = isSignedIn,
                widgetProjects = WidgetProjects(
                    isSignedIn = isSignedIn,
                    refreshOrganizations = RefreshOrganizations(organizationRepository),
                    observeOrganizations = ObserveOrganizations(organizationRepository),
                ),
                getInsights = GetInsights(projectId, repository),
                searchInsights = SearchInsights(projectId, repository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("start a new widget on no project, not the app's, listing the projects to pick from") {
            subject().uiState.value shouldBe InsightWidgetPickerUiState(
                isConnected = true,
                projects = listOf(ProjectChoice(TestProject.id, TestProject.name, TestOrganization.name)),
                projectId = null,
            )
            verifySuspend(not) { repository.getInsights(any()) }
        }

        should("keep why the projects failed to load, and list them once a retry succeeds") {
            val viewModel = subject(projectsFail = true)
            viewModel.uiState.value.projects shouldBe null
            viewModel.uiState.value.projectsError shouldBe DataError.ConnectionError
            everySuspend { organizationRepository.refreshOrganizations() } returns Either.Right(Unit)
            every { organizationRepository.observeOrganizations() } returns flowOf(listOf(TestOrganization))
            viewModel.retryProjects()
            viewModel.uiState.value.projects shouldBe listOf(
                ProjectChoice(TestProject.id, TestProject.name, TestOrganization.name),
            )
            viewModel.uiState.value.projectsError shouldBe null
        }

        should("list the insights a widget can show once a project is picked") {
            val retention = TestInsightSummary.copy(id = 11, kind = InsightKind.Retention)
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary, retention))
            val viewModel = subject()
            viewModel.onProjectSelected(TestProject.id)
            viewModel.uiState.value.insights shouldBe listOf(TestInsightSummary)
        }

        should("start on the widget's own project, and list another project's insights once it's picked") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            val viewModel = subject(widgetProject = otherProject)
            viewModel.uiState.value.projectId shouldBe otherProject
            verifySuspend { repository.getInsights(otherProject) }
            viewModel.onProjectSelected(TestSelectedProject.projectId)
            viewModel.uiState.value.projectId shouldBe TestSelectedProject.projectId
            verifySuspend { repository.getInsights(TestSelectedProject.projectId) }
        }

        should("ask to set Snout up when signed out") {
            subject(key = null).uiState.value.isConnected shouldBe false
        }

        should("search the widget's project when a query is typed") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            everySuspend { repository.searchInsights(otherProject, "sign") } returns Either.Right(listOf(searched))
            val viewModel = subject(widgetProject = otherProject)
            viewModel.onQueryChanged("sign")
            viewModel.uiState.value.searchResults shouldBe listOf(searched)
        }

        should("start another project on its whole list, without the previous project's search") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            everySuspend { repository.searchInsights(otherProject, "sign") } returns Either.Right(listOf(searched))
            val viewModel = subject(widgetProject = otherProject)
            viewModel.onQueryChanged("sign")
            viewModel.onProjectSelected(TestSelectedProject.projectId)
            viewModel.uiState.value.query shouldBe ""
            viewModel.uiState.value.searchResults shouldBe null
            viewModel.uiState.value.insights shouldBe listOf(TestInsightSummary)
        }
    })
