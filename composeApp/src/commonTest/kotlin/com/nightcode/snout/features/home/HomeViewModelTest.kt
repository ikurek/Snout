package com.nightcode.snout.features.home

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.links.FindLinkInText
import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

private const val InsightLink = "https://eu.posthog.com/project/1/insights/aBc123"

private val StartupPreferences = NavigationPreferences.Default.withLanding(HomeSection.Insights)

class HomeViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        lateinit var appStateRepository: AppStateRepository
        lateinit var incomingLinkRepository: IncomingLinkRepository

        fun subject(): HomeViewModel {
            appStateRepository = mock<AppStateRepository>(autoUnit)
            incomingLinkRepository = mock<IncomingLinkRepository>(autoUnit)
            every { appStateRepository.observeNavigationPreferences() } returns flowOf(NavigationPreferences.Default)
            val findLinkInText = FindLinkInText()
            return HomeViewModel(
                initialNavigationPreferences = StartupPreferences,
                observeNavigationPreferences = ObserveNavigationPreferences(appStateRepository),
                findLinkInText = findLinkInText,
                submitIncomingLink = SubmitIncomingLink(findLinkInText, incomingLinkRepository),
            )
        }

        should("start from the preferences read at startup") {
            subject().uiState.value.navigationPreferences shouldBe StartupPreferences
        }

        should("follow the stored navigation preferences") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.navigationPreferences shouldBe NavigationPreferences.Default
            }
        }

        should("queue the PostHog link found in pasted text and accept it") {
            val viewModel = subject()

            viewModel.onLinkSubmitted("Checkout funnel\n$InsightLink") shouldBe true

            verifySuspend { incomingLinkRepository.savePendingLink(InsightLink) }
        }

        should("reject a link Snout can't show, without queueing it") {
            val viewModel = subject()

            viewModel.onLinkSubmitted("https://eu.posthog.com/project/1/replay/home") shouldBe false

            verifySuspend(VerifyMode.not) { incomingLinkRepository.savePendingLink(any()) }
        }

        should("reject text without a link") {
            val viewModel = subject()

            viewModel.onLinkSubmitted("no link here") shouldBe false

            verifySuspend(VerifyMode.not) { incomingLinkRepository.savePendingLink(any()) }
        }
    })
