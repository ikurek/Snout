package com.nightcode.snout.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.molecules.ScreenPlaceholder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_lock_outlined_24dp
import snout.composeapp.generated.resources.placeholder_payment_required_retry
import snout.composeapp.generated.resources.placeholder_payment_required_subtitle
import snout.composeapp.generated.resources.placeholder_payment_required_title

@Composable
internal fun PaymentRequiredPlaceholder(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ScreenPlaceholder(
        icon = painterResource(Res.drawable.ic_lock_outlined_24dp),
        title = stringResource(Res.string.placeholder_payment_required_title),
        subtitle = stringResource(Res.string.placeholder_payment_required_subtitle),
        retryLabel = stringResource(Res.string.placeholder_payment_required_retry),
        onRetry = onRetry,
        modifier = modifier,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun PaymentRequiredPlaceholderPreview() = PaymentRequiredPlaceholder(onRetry = {})
