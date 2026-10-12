package com.nightcode.snout.features.onboarding

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Moves the onboarding pager between steps. The pager only moves in code, so every step change goes through here;
 * [onLeaveProjectSelection] runs when back leaves project selection, which drops the key that got the user there.
 */
@Stable
internal class OnboardingNavigator(
    private val pagerState: PagerState,
    private val scope: CoroutineScope,
    private val keyboardController: SoftwareKeyboardController?,
    private val onLeaveProjectSelection: () -> Unit,
) {
    /** The step the pager is heading to. */
    val targetStep: OnboardingStep get() = OnboardingStep.entries[pagerState.targetPage]

    /** Back is handled in the app everywhere but the intro, where it falls through to the system. */
    val canNavigateBack: Boolean get() = targetStep != OnboardingStep.Intro

    fun goTo(step: OnboardingStep, hideKeyboard: Boolean = false) {
        if (hideKeyboard) keyboardController?.hide()
        scope.launch { pagerState.animateScrollToPage(step.ordinal) }
    }

    fun navigateBack() {
        val step = targetStep
        if (step == OnboardingStep.ProjectSelection) {
            onLeaveProjectSelection()
        }
        if (step != OnboardingStep.Intro) {
            goTo(OnboardingStep.entries[step.ordinal - 1], hideKeyboard = true)
        }
    }
}

@Composable
internal fun rememberOnboardingNavigator(
    pagerState: PagerState,
    onLeaveProjectSelection: () -> Unit,
): OnboardingNavigator {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentOnLeaveProjectSelection by rememberUpdatedState(onLeaveProjectSelection)
    return remember(pagerState, scope, keyboardController) {
        OnboardingNavigator(pagerState, scope, keyboardController) { currentOnLeaveProjectSelection() }
    }
}
