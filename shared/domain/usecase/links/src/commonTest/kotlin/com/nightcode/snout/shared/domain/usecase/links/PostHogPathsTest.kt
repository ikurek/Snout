package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.links.PostHogLink
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private const val SelfHosted = "https://posthog.example.com:8443"
private const val ProjectId = 98_765L

private val EveryResource = listOf(
    PostHogResource.Project,
    PostHogResource.WebAnalytics,
    PostHogResource.Dashboards,
    PostHogResource.Dashboard(Long.MAX_VALUE),
    PostHogResource.Insights,
    PostHogResource.Insight("Zx_9-a"),
    PostHogResource.FeatureFlags,
    PostHogResource.FeatureFlag(1),
    PostHogResource.ErrorTracking,
    PostHogResource.ErrorTrackingIssue("01890ab1-2c3d-7e4f-8a9b-0c1d2e3f4a5b"),
    PostHogResource.Logs,
    PostHogResource.Experiments,
    PostHogResource.Experiment(42),
    PostHogResource.Annotations,
    PostHogResource.ActivityLog,
)

class PostHogPathsTest :
    ShouldSpec({
        should("parse every path back into its resource") {
            EveryResource.forEach { resource ->
                parsePath(resource.path().split('/').filter { it.isNotEmpty() }) shouldBe resource
            }
        }

        should("give every resource its own path") {
            EveryResource.map { it.path() }.distinct().size shouldBe EveryResource.size
        }

        should("round trip every url built for a self-hosted instance") {
            val authRepository = mock<AuthRepository>()
            val appStateRepository = mock<AppStateRepository>()
            everySuspend { authRepository.getInstanceUrl() } returns SelfHosted
            everySuspend { appStateRepository.getSelectedProject() } returns
                SelectedProject(Uuid.parse("00000000-0000-0000-0000-000000000009"), ProjectId)
            val getPostHogUrl = GetPostHogUrl(authRepository, appStateRepository)
            val parse = ParsePostHogLink()

            EveryResource.forEach { resource ->
                parse(getPostHogUrl(resource)!!) shouldBeRight PostHogLink(SelfHosted, ProjectId, resource)
            }
        }
    })
