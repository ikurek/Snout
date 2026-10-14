package com.nightcode.snout.features.onboarding

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.features.onboarding.intro.IntroPage
import com.nightcode.snout.features.onboarding.login.LoginPage
import com.nightcode.snout.features.onboarding.login.LoginSideEffect
import com.nightcode.snout.features.onboarding.login.LoginUiState
import com.nightcode.snout.features.onboarding.login.LoginViewModel
import com.nightcode.snout.features.onboarding.project.ProjectSelectionPage
import com.nightcode.snout.features.onboarding.project.ProjectSelectionSideEffect
import com.nightcode.snout.features.onboarding.project.ProjectSelectionUiState
import com.nightcode.snout.features.onboarding.project.ProjectSelectionViewModel
import com.nightcode.snout.navigation.OnboardingNavKey
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.snackbar_generic_error_message
import snout.composeapp.generated.resources.snackbar_rate_limited_message

@Composable
internal fun OnboardingRoute(navKey: OnboardingNavKey, onFinished: () -> Unit) {
    val loginViewModel = koinViewModel<LoginViewModel>()
    val projectSelectionViewModel = koinViewModel<ProjectSelectionViewModel>()
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val projectSelectionState by projectSelectionViewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = navKey.startStep.ordinal) { OnboardingStep.entries.size }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // The pager only moves in code, so every step change goes through here.
    fun goTo(step: OnboardingStep, hideKeyboard: Boolean = false) {
        if (hideKeyboard) keyboardController?.hide()
        scope.launch { pagerState.animateScrollToPage(step.ordinal) }
    }

    // Leaving project selection drops the key that got the user there.
    fun navigateBack() {
        val step = OnboardingStep.entries[pagerState.targetPage]
        if (step == OnboardingStep.ProjectSelection) projectSelectionViewModel.disconnect()
        if (step != OnboardingStep.Intro) goTo(OnboardingStep.entries[step.ordinal - 1], hideKeyboard = true)
    }

    // Owned here rather than by the login page: the fixed Connect button reads them.
    val instanceUrlTextFieldState = rememberTextFieldState()
    val personalApiKeyTextFieldState = rememberTextFieldState()
    val snackbarHostState = remember { SnackbarHostState() }

    // On the intro, back falls through to the system and leaves the app.
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = pagerState.targetPage != OnboardingStep.Intro.ordinal,
        onBackCompleted = ::navigateBack,
    )

    // Every arrival loads afresh: the key may have changed since the last visit.
    LaunchedEffect(pagerState.targetPage) {
        if (pagerState.targetPage == OnboardingStep.ProjectSelection.ordinal) {
            projectSelectionViewModel.refresh()
        }
    }

    OnboardingSideEffects(
        loginViewModel = loginViewModel,
        projectSelectionViewModel = projectSelectionViewModel,
        snackbarHostState = snackbarHostState,
        onAuthorized = { goTo(OnboardingStep.ProjectSelection, hideKeyboard = true) },
        onFinished = onFinished,
    )

    OnboardingScreen(
        pagerState = pagerState,
        snackbarHostState = snackbarHostState,
        isConnectEnabled = loginState.isConnectEnabled,
        isConnecting = loginState.isConnecting,
        onNavigateBack = ::navigateBack,
        onGetStartedClicked = { goTo(OnboardingStep.Login) },
        onConnectClicked = {
            loginViewModel.connect(
                instanceUrlTextFieldState.text.toString(),
                personalApiKeyTextFieldState.text.toString(),
            )
        },
    ) { step, bottomPadding ->
        OnboardingPage(
            step = step,
            bottomPadding = bottomPadding,
            loginState = loginState,
            loginViewModel = loginViewModel,
            projectSelectionState = projectSelectionState,
            projectSelectionViewModel = projectSelectionViewModel,
            instanceUrlTextFieldState = instanceUrlTextFieldState,
            personalApiKeyTextFieldState = personalApiKeyTextFieldState,
        )
    }
}

/** Login moves on to project selection or reports a failed connect; picking a project finishes onboarding. */
@Composable
private fun OnboardingSideEffects(
    loginViewModel: LoginViewModel,
    projectSelectionViewModel: ProjectSelectionViewModel,
    snackbarHostState: SnackbarHostState,
    onAuthorized: () -> Unit,
    onFinished: () -> Unit,
) {
    val currentOnAuthorized by rememberUpdatedState(onAuthorized)
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(loginViewModel.sideEffects) {
        loginViewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                LoginSideEffect.UserAuthorized -> currentOnAuthorized()

                is LoginSideEffect.ConnectFailed -> snackbarHostState.showSnackbar(
                    getString(
                        sideEffect.error.actionFailedMessage(
                            generic = Res.string.snackbar_generic_error_message,
                            rateLimited = Res.string.snackbar_rate_limited_message,
                        ),
                    ),
                )
            }
        }
    }

    LaunchedEffect(projectSelectionViewModel.sideEffects) {
        projectSelectionViewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                ProjectSelectionSideEffect.ProjectSelected -> currentOnFinished()
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    step: OnboardingStep,
    bottomPadding: PaddingValues,
    loginState: LoginUiState,
    loginViewModel: LoginViewModel,
    projectSelectionState: ProjectSelectionUiState,
    projectSelectionViewModel: ProjectSelectionViewModel,
    instanceUrlTextFieldState: TextFieldState,
    personalApiKeyTextFieldState: TextFieldState,
) {
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
