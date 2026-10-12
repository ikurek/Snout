package com.nightcode.snout.features.home

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.links.FindLinkInText
import com.nightcode.snout.shared.domain.usecase.links.ParsePostHogLink
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

private const val InsightLink = "https://eu.posthog.com/project/1/insights/aBc123"

class HomeViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        lateinit var appStateRepository: AppStateRepository
        lateinit var incomingLinkRepository: IncomingLinkRepository

        fun subject(): HomeViewModel {
            appStateRepository = mock<AppStateRepository>(autoUnit)
            incomingLinkRepository = mock<IncomingLinkRepository>(autoUnit)
            every { appStateRepository.observeNavigationPreferences() } returns flowOf(NavigationPreferences.Default)
            val parsePostHogLink = ParsePostHogLink()
            val findLinkInText = FindLinkInText(parsePostHogLink)
            return HomeViewModel(
                observeNavigationPreferences = ObserveNavigationPreferences(appStateRepository),
                findLinkInText = findLinkInText,
                parsePostHogLink = parsePostHogLink,
                submitIncomingLink = SubmitIncomingLink(findLinkInText, incomingLinkRepository),
            )
        }

        should("expose the stored navigation preferences") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.navigationPreferences shouldBe NavigationPreferences.Default
            }
        }

        should("queue the PostHog link found in pasted text and close the sheet") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                val effect = async(Dispatchers.Main) { viewModel.sideEffects.first() }

                viewModel.onLinkSubmitted("Checkout funnel\n$InsightLink")

                effect.await() shouldBe HomeSideEffect.LinkAccepted
                verifySuspend { incomingLinkRepository.savePendingLink(InsightLink) }
                viewModel.uiState.value.rejectedLink shouldBe null
            }
        }

        should("reject a link Snout can't show, without queueing it") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onLinkSubmitted("https://eu.posthog.com/project/1/replay/home")

                viewModel.uiState.value.rejectedLink shouldBe "https://eu.posthog.com/project/1/replay/home"
                verifySuspend(VerifyMode.not) { incomingLinkRepository.savePendingLink(any()) }
            }
        }

        should("reject text without a link, and forget the rejection once a link is accepted") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onLinkSubmitted("no link here")
                viewModel.uiState.value.rejectedLink shouldBe "no link here"

                val effect = async(Dispatchers.Main) { viewModel.sideEffects.first() }
                viewModel.onLinkSubmitted(InsightLink)
                effect.await() shouldBe HomeSideEffect.LinkAccepted
                viewModel.uiState.value.rejectedLink shouldBe null
            }
        }
    })
