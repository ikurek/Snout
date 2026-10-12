package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetPostHogUrlTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val appStateRepository = mock<AppStateRepository>()
        val subject = GetPostHogUrl(authRepository, appStateRepository)

        should("build project scoped url for a resource") {
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            subject(PostHogResource.Insight("abc123")) shouldBe "https://eu.posthog.com/project/1/insights/abc123"
            subject(PostHogResource.FeatureFlag(7)) shouldBe "https://eu.posthog.com/project/1/feature_flags/7"
        }

        should("return null when not signed in") {
            everySuspend { authRepository.getInstanceUrl() } returns null
            subject(PostHogResource.Project) shouldBe null
        }
    })
