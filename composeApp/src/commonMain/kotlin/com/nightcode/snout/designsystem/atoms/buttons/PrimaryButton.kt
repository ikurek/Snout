package com.nightcode.snout.designsystem.atoms.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.HorizontalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_right_alt_24dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1.0f)
    Button(
        onClick = onClick,
        shapes = ButtonShapes(
            shape = SnoutTheme.shapes.large,
            pressedShape = SnoutTheme.shapes.small,
        ),
        modifier = Modifier.defaultMinSize(minHeight = 48.dp * fontScale).then(modifier),
        enabled = enabled,
    ) {
        AnimatedContent(targetState = loading) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp * fontScale),
                    color = ButtonDefaults.buttonColors().contentColor,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text)
                    icon?.let {
                        HorizontalSpacer(8.dp)
                        icon()
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryButtonEnabledPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryButtonDisabledPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
    enabled = false,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryButtonEnabledWithIconPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
    icon = {
        Icon(painterResource(Res.drawable.ic_arrow_right_alt_24dp), contentDescription = null)
    },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryButtonDisabledWithIconPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
    enabled = false,
    icon = {
        Icon(painterResource(Res.drawable.ic_arrow_right_alt_24dp), contentDescription = null)
    },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryButtonEnabledLoadingPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
    loading = true,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryButtonDisabledLoadingPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
    loading = true,
)
