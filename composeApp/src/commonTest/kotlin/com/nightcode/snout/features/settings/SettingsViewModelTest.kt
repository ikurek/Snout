package com.nightcode.snout.features.settings

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.user.TestUser
import com.nightcode.snout.shared.domain.model.user.User
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.SaveThemeMode
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.links.CanOpenLinkSettings
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.links.OpenLinkSettings
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.user.ObserveCurrentUser
import com.nightcode.snout.shared.domain.usecase.user.RefreshCurrentUser
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class SettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val storedUser = MutableStateFlow<User?>(null)
        val userRepository = mock<UserRepository>(autoUnit) {
            every { observeCurrentUser() } returns storedUser
        }

        /** Answers account fetches like the repository: a success stores the account. */
        fun fetchAnswers(result: Either<DataError, User>) {
            everySuspend { userRepository.refreshCurrentUser() } calls {
                result.map { storedUser.value = it }
            }
        }
        fetchAnswers(Either.Right(TestUser))

        val authRepository = mock<AuthRepository>(autoUnit)
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit)
        val externalAppLauncher = mock<ExternalAppLauncher>(autoUnit) {
            every { canOpenLinkSettings } returns true
        }

        fun subject(): SettingsViewModel {
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            every { organizationRepository.observeOrganizations() } returns flowOf(listOf(TestOrganization))
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { appStateRepository.observeThemeMode() } returns flowOf(ThemeMode.Dark)
            return SettingsViewModel(
                refreshCurrentUser = RefreshCurrentUser(userRepository),
                observeCurrentUser = ObserveCurrentUser(userRepository),
                getInstanceUrl = GetInstanceUrl(authRepository),
                saveThemeMode = SaveThemeMode(appStateRepository),
                logout = Logout(
                    authRepository,
                    ClearLocalData(appStateRepository, organizationRepository, userRepository),
                ),
                observeOrganizations = ObserveOrganizations(organizationRepository),
                observeSelectedProject = ObserveSelectedProject(appStateRepository),
                observeThemeMode = ObserveThemeMode(appStateRepository),
                openInBrowser = OpenInBrowser(externalAppLauncher),
                openLinkSettings = OpenLinkSettings(externalAppLauncher),
                canOpenLinkSettings = CanOpenLinkSettings(externalAppLauncher),
            )
        }

        should("expose account, connection and theme once loaded") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe SettingsUiState(
                    account = TestUser,
                    instanceUrl = "https://eu.posthog.com",
                    organizationName = TestOrganization.name,
                    projectName = TestOrganization.projects.first().name,
                    themeMode = ThemeMode.Dark,
                    canOpenLinkSettings = true,
                )
            }
        }

        should("hand the instance to the browser and open the link settings") {
            val viewModel = subject()
            viewModel.openInstance("https://eu.posthog.com")
            viewModel.openLinkSettings()
            verify { externalAppLauncher.openInBrowser("https://eu.posthog.com") }
            verify { externalAppLauncher.openLinkSettings() }
        }

        should("show the stored account and keep it when the background refresh fails") {
            storedUser.value = TestUser
            fetchAnswers(Either.Left(DataError.ConnectionError))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.account shouldBe TestUser
                verifySuspend { userRepository.refreshCurrentUser() }
            }
        }

        should("expose account error without a stored account and allow retry") {
            fetchAnswers(Either.Left(DataError.ConnectionError))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.account shouldBe null
                viewModel.uiState.value.accountError shouldBe DataError.ConnectionError

                fetchAnswers(Either.Right(TestUser))
                viewModel.refresh()
                viewModel.uiState.value.account shouldBe TestUser
            }
        }

        should("persist selected theme mode") {
            subject().onThemeModeSelected(ThemeMode.Light)
            verifySuspend { appStateRepository.saveThemeMode(ThemeMode.Light) }
        }

        should("clear local data and credentials on logout") {
            subject().onLogoutConfirmed()
            verifySuspend {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                userRepository.clear()
                authRepository.clear()
            }
        }
    })
