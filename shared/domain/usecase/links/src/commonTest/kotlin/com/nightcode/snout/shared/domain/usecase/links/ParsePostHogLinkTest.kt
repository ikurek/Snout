package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.links.PostHogLink
import com.nightcode.snout.shared.domain.model.links.PostHogLinkError
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

private const val IssueId = "01890ab1-2c3d-7e4f-8a9b-0c1d2e3f4a5b"

@OptIn(TestData::class)
class ParsePostHogLinkTest :
    ShouldSpec({
        val subject = ParsePostHogLink()

        should("parse every link Snout builds back into the same resource") {
            val authRepository = mock<AuthRepository>()
            val appStateRepository = mock<AppStateRepository>()
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            val getPostHogUrl = GetPostHogUrl(authRepository, appStateRepository)

            listOf(
                PostHogResource.Project,
                PostHogResource.WebAnalytics,
                PostHogResource.Dashboards,
                PostHogResource.Dashboard(12),
                PostHogResource.Insights,
                PostHogResource.Insight("aBc123"),
                PostHogResource.FeatureFlags,
                PostHogResource.FeatureFlag(7),
                PostHogResource.ErrorTracking,
                PostHogResource.ErrorTrackingIssue(IssueId),
                PostHogResource.Logs,
                PostHogResource.Experiments,
                PostHogResource.Experiment(3),
                PostHogResource.Annotations,
                PostHogResource.ActivityLog,
            ).forEach { resource ->
                subject(getPostHogUrl(resource)!!) shouldBeRight PostHogLink(
                    instanceUrl = "https://eu.posthog.com",
                    projectId = TestSelectedProject.projectId,
                    resource = resource,
                )
            }
        }

        should("ignore query, fragment, trailing slash and the insight editor suffix") {
            subject("https://us.posthog.com/project/42/dashboard/5/?foo=bar#tile").shouldBeRight().resource shouldBe
                PostHogResource.Dashboard(5)
            subject(
                "https://us.posthog.com/project/42/insights/xYz9/edit?dashboard=3",
            ).shouldBeRight().resource shouldBe
                PostHogResource.Insight("xYz9")
            subject("  https://US.PostHog.com/project/42  ") shouldBeRight
                PostHogLink("https://us.posthog.com", 42, PostHogResource.Project)
        }

        should("keep a self-hosted host and port") {
            subject("https://posthog.example.com:8443/project/2/feature_flags/9") shouldBeRight
                PostHogLink("https://posthog.example.com:8443", 2, PostHogResource.FeatureFlag(9))
        }

        should("reject links that are not project pages") {
            listOf(
                "",
                "not a link",
                "http://us.posthog.com/project/1/insights",
                "https://us.posthog.com",
                "https://us.posthog.com/insights/abc",
                "https://us.posthog.com/project/abc/insights",
            ).forEach { subject(it) shouldBeLeft PostHogLinkError.NotAProjectLink }
        }

        should("reject project pages Snout can't show") {
            listOf(
                "https://us.posthog.com/project/1/replay/123",
                "https://us.posthog.com/project/1/persons",
                "https://us.posthog.com/project/1/sql",
                "https://us.posthog.com/project/1/insights/new",
                "https://us.posthog.com/project/1/feature_flags/new",
                "https://us.posthog.com/project/1/dashboard/12/extra",
                "https://us.posthog.com/project/1/error_tracking/configuration",
                "https://us.posthog.com/project/1/data-management/events",
                "https://us.posthog.com/project/1/web/web-vitals",
            ).forEach { subject(it) shouldBeLeft PostHogLinkError.UnsupportedPage }
        }
    })
