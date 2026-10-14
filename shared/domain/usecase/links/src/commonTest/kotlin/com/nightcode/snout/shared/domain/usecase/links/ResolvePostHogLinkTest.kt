package com.nightcode.snout.shared.domain.usecase.links

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.links.DeepLinkDestination
import com.nightcode.snout.shared.domain.model.links.LinkResolution
import com.nightcode.snout.shared.domain.model.organization.Project
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.insights.FindInsightIdByShortId
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

private const val Instance = "https://eu.posthog.com"
private const val IssueId = "01890ab1-2c3d-7e4f-8a9b-0c1d2e3f4a5b"

@OptIn(TestData::class)
class ResolvePostHogLinkTest :
    ShouldSpec({
        val otherProject = Project(id = 2L, name = "Other")
        val organizations = listOf(TestOrganization.copy(projects = TestOrganization.projects + otherProject))

        val authRepository = mock<AuthRepository>()
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>()
        val insightRepository = mock<InsightRepository>()

        val subject = ResolvePostHogLink(
            getInstanceUrl = GetInstanceUrl(authRepository),
            getSelectedProject = GetSelectedProject(appStateRepository),
            observeOrganizations = ObserveOrganizations(organizationRepository),
            refreshOrganizations = RefreshOrganizations(organizationRepository),
            saveSelectedProject = SaveSelectedProject(appStateRepository),
            findInsightIdByShortId = FindInsightIdByShortId(
                GetSelectedProjectId(appStateRepository),
                insightRepository,
            ),
        )

        beforeTest {
            everySuspend { authRepository.getInstanceUrl() } returns Instance
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { organizationRepository.observeOrganizations() } returns flowOf(organizations)
            everySuspend { organizationRepository.refreshOrganizations() } returns Either.Right(Unit)
        }

        should("open a detail page of the selected project") {
            subject("$Instance/project/1/dashboard/5") shouldBe
                LinkResolution.InApp(DeepLinkDestination.Dashboard(5))
            subject("$Instance/project/1/error_tracking/$IssueId?fingerprint=x") shouldBe
                LinkResolution.InApp(DeepLinkDestination.ErrorTrackingIssue(IssueId))
        }

        should("open a list page as its section and the project root as Home") {
            subject("$Instance/project/1/insights") shouldBe
                LinkResolution.InApp(DeepLinkDestination.Section(HomeSection.Insights))
            subject("$Instance/project/1/data-management/annotations") shouldBe
                LinkResolution.InApp(DeepLinkDestination.Section(HomeSection.Annotations))
            subject("$Instance/project/1") shouldBe LinkResolution.InApp(DeepLinkDestination.Home)
        }

        should("look an insight up by its short id") {
            everySuspend { insightRepository.findInsightId(1L, "aBc123") } returns Either.Right(42L)
            subject("$Instance/project/1/insights/aBc123") shouldBe
                LinkResolution.InApp(DeepLinkDestination.Insight(42L))
        }

        should("send an insight the API can't find to the browser") {
            everySuspend { insightRepository.findInsightId(1L, "gone") } returns Either.Right(null)
            everySuspend { insightRepository.findInsightId(1L, "offline") } returns
                Either.Left(DataError.ConnectionError)
            subject("$Instance/project/1/insights/gone") shouldBe
                LinkResolution.Browser("$Instance/project/1/insights/gone")
            subject("$Instance/project/1/insights/offline") shouldBe
                LinkResolution.Browser("$Instance/project/1/insights/offline")
        }

        should("switch to another project the user can access") {
            subject("$Instance/project/2/feature_flags") shouldBe
                LinkResolution.InApp(DeepLinkDestination.Section(HomeSection.FeatureFlags))
            verifySuspend { appStateRepository.saveSelectedProject(TestOrganization.id, 2L) }
        }

        should("send a project the user can't access to the browser after refreshing organizations") {
            subject("$Instance/project/99/feature_flags") shouldBe
                LinkResolution.Browser("$Instance/project/99/feature_flags")
            verifySuspend { organizationRepository.refreshOrganizations() }
            verifySuspend(not) { appStateRepository.saveSelectedProject(TestOrganization.id, 99L) }
        }

        should("send links to another instance to the browser") {
            subject("https://us.posthog.com/project/1/dashboard/5") shouldBe
                LinkResolution.Browser("https://us.posthog.com/project/1/dashboard/5")
        }

        should("treat app.posthog.com as the US cloud") {
            everySuspend { authRepository.getInstanceUrl() } returns "https://us.posthog.com"
            subject("https://app.posthog.com/project/1/dashboard/5") shouldBe
                LinkResolution.InApp(DeepLinkDestination.Dashboard(5))
        }

        should("send pages Snout can't show and other web links to the browser") {
            subject("$Instance/project/1/replay/home") shouldBe
                LinkResolution.Browser("$Instance/project/1/replay/home")
            subject("https://example.com/page") shouldBe LinkResolution.Browser("https://example.com/page")
        }
    })
