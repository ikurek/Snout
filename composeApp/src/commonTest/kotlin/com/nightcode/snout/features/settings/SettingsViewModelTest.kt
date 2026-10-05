package com.nightcode.snout.features.settings

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.model.user.TestUser
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.SaveThemeMode
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.user.GetCurrentUser
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(TestData::class)
class SettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val userRepository = mock<UserRepository>()
        val authRepository = mock<AuthRepository>(autoUnit)
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit)

        fun subject(): SettingsViewModel {
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            every { organizationRepository.observeOrganizations() } returns flowOf(listOf(TestOrganization))
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { appStateRepository.observeThemeMode() } returns flowOf(ThemeMode.Dark)
            return SettingsViewModel(
                getCurrentUser = GetCurrentUser(userRepository),
                getInstanceUrl = GetInstanceUrl(authRepository),
                saveThemeMode = SaveThemeMode(appStateRepository),
                logout = Logout(authRepository, ClearLocalData(appStateRepository, organizationRepository)),
                observeOrganizations = ObserveOrganizations(organizationRepository),
                observeSelectedProject = ObserveSelectedProject(appStateRepository),
                observeThemeMode = ObserveThemeMode(appStateRepository),
            )
        }

        should("expose account, connection and theme once loaded") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Right(TestUser)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value shouldBe SettingsUiState(
                account = AccountUiState.Loaded(TestUser),
                instanceUrl = "https://eu.posthog.com",
                organizationName = TestOrganization.name,
                projectName = TestOrganization.projects.first().name,
                themeMode = ThemeMode.Dark,
            )
            collector.cancel()
        }

        should("expose account error and allow retry") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Left(GetCurrentUserError.ConnectionError)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
            viewModel.uiState.value.account shouldBe AccountUiState.Error(GetCurrentUserError.ConnectionError)

            everySuspend { userRepository.getCurrentUser() } returns Either.Right(TestUser)
            viewModel.loadAccount()
            viewModel.uiState.value.account shouldBe AccountUiState.Loaded(TestUser)
            collector.cancel()
        }

        should("persist selected theme mode") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Right(TestUser)
            subject().onThemeModeSelected(ThemeMode.Light)
            verifySuspend { appStateRepository.saveThemeMode(ThemeMode.Light) }
        }

        should("clear local data and credentials on logout") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Right(TestUser)
            subject().onLogoutConfirmed()
            verifySuspend {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                authRepository.clear()
            }
        }
    })
