package com.nightcode.snout.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.onboarding.intro.IntroPage
import com.nightcode.snout.features.onboarding.login.LoginPage
import com.nightcode.snout.features.onboarding.project.ProjectSelectionPage
import com.nightcode.snout.features.onboarding.project.ProjectSelectionUiState
import com.nightcode.snout.features.onboarding.project.TestProjectSelectionUiStateLoaded
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_right_alt_24dp
import snout.composeapp.generated.resources.intro_screen_get_started_button
import snout.composeapp.generated.resources.login_screen_connect_button_text
import snout.composeapp.generated.resources.onboarding_step_content_description

/** The three steps between a fresh install (or a logout) and Home, in pager order. */
@Serializable
internal enum class OnboardingStep { Intro, Login, ProjectSelection }

private val TopBarHeight = 56.dp
private val IconButtonSize = 48.dp
private val SegmentHeight = 4.dp

/**
 * One screen for the whole onboarding: the back arrow, step progress and bottom button stay put while the steps
 * slide underneath. The pager only moves in code, so nobody reaches project selection without connecting.
 * [page] draws a step; its bottom padding clears the button, or the navigation bar once the button is gone.
 */
@Composable
internal fun OnboardingScreen(
    pagerState: PagerState,
    snackbarHostState: SnackbarHostState,
    isConnectEnabled: Boolean,
    isConnecting: Boolean,
    onNavigateBack: () -> Unit,
    onGetStartedClick: () -> Unit,
    onConnectClick: () -> Unit,
    page: @Composable (step: OnboardingStep, bottomPadding: PaddingValues) -> Unit,
) {
    // The step the pager is heading to, so the arrow and button change as soon as a move starts.
    val step = OnboardingStep.entries[pagerState.targetPage]
    Scaffold(
        // safeDrawing includes the IME; the bottom bar takes the bottom side so its button rides above the keyboard.
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopBar(pagerState = pagerState, showBack = step != OnboardingStep.Intro, onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            BottomBar(
                step = step,
                isConnectEnabled = isConnectEnabled,
                isConnecting = isConnecting,
                onGetStartedClick = onGetStartedClick,
                onConnectClick = onConnectClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        val pagerPadding = contentPadding.withoutBottom()
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(pagerPadding)
                .consumeWindowInsets(pagerPadding),
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top,
        ) { index ->
            page(OnboardingStep.entries[index], contentPadding.bottomOnly())
        }
    }
}

/** Back arrow (fading in after the intro) on the left, the progress centred between it and a matching gap. */
@Composable
private fun TopBar(pagerState: PagerState, showBack: Boolean, onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .height(TopBarHeight)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val backAlpha by animateFloatAsState(if (showBack) 1f else 0f)
        Box(modifier = Modifier.size(IconButtonSize), contentAlignment = Alignment.Center) {
            if (backAlpha > 0f) {
                NavigationBackIconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.graphicsLayer { alpha = backAlpha },
                )
            }
        }
        StepProgress(pagerState = pagerState, modifier = Modifier.weight(1f).padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.size(IconButtonSize))
    }
}

/**
 * One rounded segment per step, filled up to the pager's position: as a step slides in its segment fills with it,
 * and going back drains it. Read by screen readers as "Step 2 of 3".
 */
@Composable
private fun StepProgress(pagerState: PagerState, modifier: Modifier = Modifier) {
    val description = stringResource(
        Res.string.onboarding_step_content_description,
        pagerState.targetPage + 1,
        OnboardingStep.entries.size,
    )
    val fillColor = SnoutTheme.colors.primary
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OnboardingStep.entries.forEach { segment ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(SegmentHeight)
                    .clip(CircleShape)
                    .background(SnoutTheme.colors.surfaceContainerHighest)
                    .drawBehind {
                        // Read in the draw phase so following the pager never recomposes the bar.
                        val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
                        val fill = (position - segment.ordinal + 1).coerceIn(0f, 1f)
                        if (fill > 0f) {
                            drawRoundRect(
                                color = fillColor,
                                size = Size(size.width * fill, size.height),
                                cornerRadius = CornerRadius(size.height / 2),
                            )
                        }
                    },
            )
        }
    }
}

/**
 * The step's one action, fixed while the pages slide: the same button turns from "Get started" into "Connect",
 * and the bar leaves on project selection, where tapping a project is the action.
 */
@Composable
private fun BottomBar(
    step: OnboardingStep,
    isConnectEnabled: Boolean,
    isConnecting: Boolean,
    onGetStartedClick: () -> Unit,
    onConnectClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = step != OnboardingStep.ProjectSelection,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .padding(16.dp),
        ) {
            // One button for both steps, so its surface stays put and only the label changes.
            val isIntro = step == OnboardingStep.Intro
            val label = when {
                isIntro -> Res.string.intro_screen_get_started_button
                else -> Res.string.login_screen_connect_button_text
            }
            PrimaryButton(
                text = stringResource(label),
                onClick = if (isIntro) onGetStartedClick else onConnectClick,
                modifier = Modifier.fillMaxWidth(),
                icon = painterResource(Res.drawable.ic_arrow_right_alt_24dp),
                enabled = isIntro || isConnectEnabled,
                loading = isIntro.not() && isConnecting,
            )
        }
    }
}

@OptIn(TestData::class)
@Composable
private fun OnboardingPreview(
    step: OnboardingStep,
    instanceUrlValidationError: InstanceUrlValidationError? = null,
    personalApiKeyValidationError: PersonalApiKeyValidationError? = null,
    isConnecting: Boolean = false,
    projectSelectionState: ProjectSelectionUiState = TestProjectSelectionUiStateLoaded,
) {
    val instanceUrl = rememberTextFieldState()
    val personalApiKey = rememberTextFieldState()
    OnboardingScreen(
        pagerState = rememberPagerState(initialPage = step.ordinal) { OnboardingStep.entries.size },
        snackbarHostState = remember { SnackbarHostState() },
        isConnectEnabled = isConnecting.not(),
        isConnecting = isConnecting,
        onNavigateBack = {},
        onGetStartedClick = {},
        onConnectClick = {},
    ) { page, bottomPadding ->
        when (page) {
            OnboardingStep.Intro -> IntroPage(bottomPadding = bottomPadding)

            OnboardingStep.Login -> LoginPage(
                instanceUrlTextFieldState = instanceUrl,
                personalApiKeyTextFieldState = personalApiKey,
                savedInstanceUrl = null,
                instanceUrlValidationError = instanceUrlValidationError,
                personalApiKeyValidationError = personalApiKeyValidationError,
                isConnecting = isConnecting,
                bottomPadding = bottomPadding,
                onInstanceUrlChange = {},
                onPersonalApiKeyChange = {},
                onApiKeysLinkClick = {},
            )

            OnboardingStep.ProjectSelection -> ProjectSelectionPage(
                state = projectSelectionState,
                bottomPadding = bottomPadding,
                onRetry = {},
                onProjectSelected = { _, _ -> },
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingIntroPreview() = OnboardingPreview(step = OnboardingStep.Intro)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingLoginPreview() = OnboardingPreview(step = OnboardingStep.Login)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingLoginErrorsPreview() = OnboardingPreview(
    step = OnboardingStep.Login,
    instanceUrlValidationError = InstanceUrlValidationError.UrlIsInvalid,
    personalApiKeyValidationError = PersonalApiKeyValidationError.ServerValidationFailed,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingLoginConnectingPreview() = OnboardingPreview(step = OnboardingStep.Login, isConnecting = true)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingProjectSelectionPreview() = OnboardingPreview(step = OnboardingStep.ProjectSelection)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingProjectSelectionLoadingPreview() = OnboardingPreview(
    step = OnboardingStep.ProjectSelection,
    projectSelectionState = ProjectSelectionUiState.Loading,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OnboardingProjectSelectionErrorPreview() = OnboardingPreview(
    step = OnboardingStep.ProjectSelection,
    projectSelectionState = ProjectSelectionUiState.Error(DataError.UnexpectedError),
)
