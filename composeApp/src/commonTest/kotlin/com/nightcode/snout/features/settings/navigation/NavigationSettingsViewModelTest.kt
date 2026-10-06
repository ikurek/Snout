package com.nightcode.snout.features.settings.navigation

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.SaveNavigationPreferences
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class NavigationSettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val repository = mock<AppStateRepository>(autoUnit)

        fun subject(): NavigationSettingsViewModel {
            every { repository.observeNavigationPreferences() } returns flowOf(NavigationPreferences.Default)
            return NavigationSettingsViewModel(
                saveNavigationPreferences = SaveNavigationPreferences(repository),
                observeNavigationPreferences = ObserveNavigationPreferences(repository),
            )
        }

        should("expose stored preferences") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
            viewModel.uiState.value shouldBe NavigationPreferences.Default
            collector.cancel()
        }

        should("save a swapped slot") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
            viewModel.onSlotSelected(0, HomeSection.Dashboards)
            verifySuspend {
                repository.saveNavigationPreferences(
                    NavigationPreferences(
                        tabs = listOf(
                            HomeSection.Dashboards,
                            HomeSection.Analytics,
                            HomeSection.Insights,
                            HomeSection.FeatureFlags,
                        ),
                        landing = HomeSection.Analytics,
                    ),
                )
            }
            collector.cancel()
        }

        should("save a new landing tab") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
            viewModel.onLandingSelected(HomeSection.Insights)
            verifySuspend {
                repository.saveNavigationPreferences(NavigationPreferences.Default.copy(landing = HomeSection.Insights))
            }
            collector.cancel()
        }
    })
