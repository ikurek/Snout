package com.nightcode.snout.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextField
import com.nightcode.snout.designsystem.atoms.textfields.TextFieldError
import com.nightcode.snout.designsystem.organisms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.open_link_button
import snout.composeapp.generated.resources.open_link_description
import snout.composeapp.generated.resources.open_link_error
import snout.composeapp.generated.resources.open_link_label
import snout.composeapp.generated.resources.open_link_placeholder
import snout.composeapp.generated.resources.open_link_title

/**
 * Paste a PostHog link to open its page in the app; the way in on iOS, which can't claim posthog.com links.
 * [onOpen] asks to open what's in [linkState]; [showError] says it was already turned down.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenLinkSheet(
    isVisible: Boolean,
    linkState: TextFieldState,
    showError: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
) {
    AnimatedBottomSheet(isVisible = isVisible, onDismissRequest = onDismiss) {
        OpenLinkSheetContent(linkState = linkState, showError = showError, onOpen = onOpen)
    }
}

@Composable
private fun OpenLinkSheetContent(
    linkState: TextFieldState,
    showError: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Spacing.large).imePadding()) {
        Text(text = stringResource(Res.string.open_link_title), style = SnoutTheme.typography.titleLarge)
        VerticalSpacer(Spacing.xxSmall)
        Text(
            text = stringResource(Res.string.open_link_description),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
        VerticalSpacer(Spacing.large)
        PrimaryTextField(
            state = linkState,
            label = stringResource(Res.string.open_link_label),
            placeholder = stringResource(Res.string.open_link_placeholder),
            error = if (showError) TextFieldError.Resource(Res.string.open_link_error) else null,
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(Spacing.large)
        PrimaryButton(
            text = stringResource(Res.string.open_link_button),
            onClick = onOpen,
            enabled = linkState.text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(Spacing.xxLarge)
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OpenLinkSheetPreview() = OpenLinkSheetContent(
    linkState = rememberTextFieldState(initialText = "https://eu.posthog.com/project/1/insights/aBc123"),
    showError = false,
    onOpen = {},
    modifier = Modifier.background(SnoutTheme.colors.surfaceContainer),
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OpenLinkSheetErrorPreview() = OpenLinkSheetContent(
    linkState = rememberTextFieldState(initialText = "https://eu.posthog.com/project/1/replay/home"),
    showError = true,
    onOpen = {},
    modifier = Modifier.background(SnoutTheme.colors.surfaceContainer),
)
