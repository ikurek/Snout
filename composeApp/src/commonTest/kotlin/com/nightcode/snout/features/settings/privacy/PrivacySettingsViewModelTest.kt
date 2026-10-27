package com.nightcode.snout.features.settings.privacy

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.telemetry.IsTelemetryAvailable
import com.nightcode.snout.shared.domain.usecase.telemetry.ObserveTelemetrySettings
import com.nightcode.snout.shared.domain.usecase.telemetry.SaveTelemetrySettings
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

class PrivacySettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val repository = mock<TelemetryRepository>(autoUnit)
        val externalAppLauncher = mock<ExternalAppLauncher>(autoUnit)

        fun subject(canReport: Boolean = true): PrivacySettingsViewModel {
            every { repository.observeSettings() } returns flowOf(TelemetrySettings.Default)
            every { repository.isAvailable() } returns canReport
            return PrivacySettingsViewModel(
                saveTelemetrySettings = SaveTelemetrySettings(repository),
                openInBrowser = OpenInBrowser(externalAppLauncher),
                observeTelemetrySettings = ObserveTelemetrySettings(repository),
                isTelemetryAvailable = IsTelemetryAvailable(repository),
            )
        }

        should("expose the stored settings in a build that reports") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe PrivacySettingsUiState(TelemetrySettings.Default, canReport = true)
            }
        }

        should("say when this build can't report") {
            val viewModel = subject(canReport = false)
            viewModel.uiState.subscribed {
                viewModel.uiState.value.canReport shouldBe false
            }
        }

        should("switch each kind of report on its own") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onUsageToggled(false)
                verifySuspend { repository.saveSettings(TelemetrySettings(usage = false, errors = true)) }

                viewModel.onErrorsToggled(false)
                verifySuspend { repository.saveSettings(TelemetrySettings(usage = true, errors = false)) }
            }
        }

        should("open the privacy policy in the browser") {
            subject().onPrivacyPolicyClicked()

            verify { externalAppLauncher.openInBrowser(PrivacyPolicyUrl) }
        }
    })
