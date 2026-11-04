package com.nightcode.snout.features.home

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

private val StartupPreferences = NavigationPreferences.Default.withLanding(HomeSection.Insights)

class HomeViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        lateinit var appStateRepository: AppStateRepository

        fun subject(): HomeViewModel {
            appStateRepository = mock<AppStateRepository>(autoUnit)
            every { appStateRepository.observeNavigationPreferences() } returns flowOf(NavigationPreferences.Default)
            return HomeViewModel(
                initialNavigationPreferences = StartupPreferences,
                observeNavigationPreferences = ObserveNavigationPreferences(appStateRepository),
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
    })
