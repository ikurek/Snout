package com.nightcode.snout.features.settings.navigation

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.SaveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackNavigationChanged
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

class NavigationSettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val telemetryRepository = mock<TelemetryRepository>(MockMode.autoUnit)

        val repository = mock<AppStateRepository>(autoUnit)

        fun subject(): NavigationSettingsViewModel {
            every { repository.observeNavigationPreferences() } returns flowOf(NavigationPreferences.Default)
            return NavigationSettingsViewModel(
                saveNavigationPreferences = SaveNavigationPreferences(repository),
                trackNavigationChanged = TrackNavigationChanged(telemetryRepository),
                observeNavigationPreferences = ObserveNavigationPreferences(repository),
            )
        }

        should("expose stored preferences") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe NavigationPreferences.Default
            }
        }

        should("save a swapped slot") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
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
            }
        }

        should("save a new landing tab") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onLandingSelected(HomeSection.Insights)
                verifySuspend {
                    repository.saveNavigationPreferences(
                        NavigationPreferences.Default.copy(landing = HomeSection.Insights),
                    )
                }
            }
        }
    })
