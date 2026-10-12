package com.nightcode.snout.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextField
import com.nightcode.snout.designsystem.atoms.textfields.TextFieldError
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.usecase.links.FindLinkInText
import com.nightcode.snout.shared.domain.usecase.links.ParsePostHogLink
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.open_link_button
import snout.composeapp.generated.resources.open_link_description
import snout.composeapp.generated.resources.open_link_error
import snout.composeapp.generated.resources.open_link_label
import snout.composeapp.generated.resources.open_link_placeholder
import snout.composeapp.generated.resources.open_link_title

/**
 * Paste a PostHog link to open its page in the app; the way in on iOS, which can't claim posthog.com links.
 * [onOpen] gets the link once it parses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenLinkSheet(isVisible: Boolean, onDismiss: () -> Unit, onOpen: (url: String) -> Unit) {
    val parsePostHogLink = koinInject<ParsePostHogLink>()
    val findLinkInText = koinInject<FindLinkInText>()
    val linkState = rememberTextFieldState()
    // The text that failed to parse; the error clears as soon as the user edits it.
    var rejected by rememberSaveable { mutableStateOf<String?>(null) }

    AnimatedBottomSheet(isVisible = isVisible, onDismissRequest = onDismiss) {
        OpenLinkSheetContent(
            linkState = linkState,
            showError = rejected != null && rejected == linkState.text.toString(),
            onOpen = {
                val text = linkState.text.toString()
                // Only pages Snout can show: the sheet isn't a way to open the browser.
                val url = findLinkInText(text)?.takeIf { parsePostHogLink(it).isRight() }
                if (url == null) {
                    rejected = text
                } else {
                    rejected = null
                    linkState.clearText()
                    onOpen(url)
                }
            },
        )
    }
}

@Composable
private fun OpenLinkSheetContent(
    linkState: TextFieldState,
    showError: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp).imePadding()) {
        Text(text = stringResource(Res.string.open_link_title), style = SnoutTheme.typography.titleLarge)
        VerticalSpacer(4.dp)
        Text(
            text = stringResource(Res.string.open_link_description),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
        VerticalSpacer(16.dp)
        PrimaryTextField(
            state = linkState,
            label = stringResource(Res.string.open_link_label),
            placeholder = stringResource(Res.string.open_link_placeholder),
            error = if (showError) TextFieldError.Resource(Res.string.open_link_error) else null,
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(16.dp)
        PrimaryButton(
            text = stringResource(Res.string.open_link_button),
            onClick = onOpen,
            enabled = linkState.text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(24.dp)
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
