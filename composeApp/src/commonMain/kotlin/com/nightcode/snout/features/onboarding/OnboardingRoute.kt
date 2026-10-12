package com.nightcode.snout.features.onboarding

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.features.onboarding.intro.IntroPage
import com.nightcode.snout.features.onboarding.login.LoginPage
import com.nightcode.snout.features.onboarding.login.LoginSideEffect
import com.nightcode.snout.features.onboarding.login.LoginViewModel
import com.nightcode.snout.features.onboarding.project.ProjectSelectionPage
import com.nightcode.snout.features.onboarding.project.ProjectSelectionSideEffect
import com.nightcode.snout.features.onboarding.project.ProjectSelectionViewModel
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.snackbar_generic_error_message
import snout.composeapp.generated.resources.snackbar_rate_limited_message

@Suppress("LongMethod")
@Composable
internal fun OnboardingRoute(navKey: OnboardingNavKey, onFinished: () -> Unit) {
    val loginViewModel = koinViewModel<LoginViewModel>()
    val projectSelectionViewModel = koinViewModel<ProjectSelectionViewModel>()
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val projectSelectionState by projectSelectionViewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = navKey.startStep.ordinal) { OnboardingStep.entries.size }
    // Owned here rather than by the login page: the fixed Connect button reads them.
    val instanceUrlTextFieldState = rememberTextFieldState()
    val personalApiKeyTextFieldState = rememberTextFieldState()
    val snackbarHostState = remember { SnackbarHostState() }
    val openInBrowser = koinInject<OpenInBrowser>()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val goTo: (OnboardingStep) -> Unit = { step -> scope.launch { pagerState.animateScrollToPage(step.ordinal) } }
    val navigateBack: () -> Unit = {
        val step = OnboardingStep.entries[pagerState.targetPage]
        if (step == OnboardingStep.ProjectSelection) {
            projectSelectionViewModel.disconnect()
        }
        if (step != OnboardingStep.Intro) {
            keyboardController?.hide()
            goTo(OnboardingStep.entries[step.ordinal - 1])
        }
    }
    // On the intro, back falls through to the system and leaves the app.
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = pagerState.targetPage > 0,
        onBackCompleted = navigateBack,
    )

    // Every arrival loads afresh: the key may have changed since the last visit.
    LaunchedEffect(pagerState.targetPage) {
        if (pagerState.targetPage == OnboardingStep.ProjectSelection.ordinal) {
            projectSelectionViewModel.refresh()
        }
    }

    LaunchedEffect(loginViewModel.sideEffects) {
        loginViewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                LoginSideEffect.UserAuthorized -> {
                    keyboardController?.hide()
                    goTo(OnboardingStep.ProjectSelection)
                }

                is LoginSideEffect.ConnectFailed -> snackbarHostState.showSnackbar(
                    getString(
                        sideEffect.error.actionFailedMessage(
                            generic = Res.string.snackbar_generic_error_message,
                            rateLimited = Res.string.snackbar_rate_limited_message,
                        ),
                    ),
                )

                is LoginSideEffect.OpenUrl -> openInBrowser(sideEffect.url)
            }
        }
    }

    LaunchedEffect(projectSelectionViewModel.sideEffects) {
        projectSelectionViewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                ProjectSelectionSideEffect.ProjectSelected -> onFinished()
            }
        }
    }

    OnboardingScreen(
        pagerState = pagerState,
        snackbarHostState = snackbarHostState,
        isConnectEnabled = loginState.isConnectEnabled,
        isConnecting = loginState.isConnecting,
        onNavigateBack = navigateBack,
        onGetStartedClicked = { goTo(OnboardingStep.Login) },
        onConnectClicked = {
            loginViewModel.connect(
                instanceUrlTextFieldState.text.toString(),
                personalApiKeyTextFieldState.text.toString(),
            )
        },
    ) { step, bottomPadding ->
        when (step) {
            OnboardingStep.Intro -> IntroPage(bottomPadding = bottomPadding)

            OnboardingStep.Login -> LoginPage(
                instanceUrlTextFieldState = instanceUrlTextFieldState,
                personalApiKeyTextFieldState = personalApiKeyTextFieldState,
                savedInstanceUrl = loginState.savedInstanceUrl,
                instanceUrlValidationError = loginState.instanceUrlValidationError,
                personalApiKeyValidationError = loginState.personalApiKeyValidationError,
                isConnecting = loginState.isConnecting,
                bottomPadding = bottomPadding,
                onInstanceUrlChanged = loginViewModel::onInstanceUrlChanged,
                onPersonalApiKeyChanged = loginViewModel::onPersonalApiKeyChanged,
                onApiKeysLinkClicked = loginViewModel::openApiKeySettings,
            )

            OnboardingStep.ProjectSelection -> ProjectSelectionPage(
                state = projectSelectionState,
                bottomPadding = bottomPadding,
                onRetry = projectSelectionViewModel::refresh,
                onProjectSelected = projectSelectionViewModel::onProjectSelected,
            )
        }
    }
}

/** [startStep] is where the pager opens: the intro after a logout, login after a 401, a project after a restart. */
@Serializable
internal data class OnboardingNavKey(val startStep: OnboardingStep) : NavKey
