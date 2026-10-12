package com.nightcode.snout.designsystem.atoms.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.HorizontalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_right_alt_24dp

internal val ButtonHeight = 44.dp
internal val ButtonIconSize = 18.dp
internal val ButtonProgressSize = 20.dp

/** The one filled action on a screen: Connect, placeholder actions, Add annotation. Optional trailing icon. */
@Composable
internal fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1.0f)
    Button(
        onClick = onClick,
        shape = SnoutTheme.shapes.medium,
        modifier = Modifier.defaultMinSize(minHeight = ButtonHeight * fontScale).then(modifier),
        enabled = enabled && loading.not(),
    ) {
        AnimatedContent(targetState = loading) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonProgressSize * fontScale),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text)
                    icon?.let {
                        HorizontalSpacer(Spacing.small)
                        Icon(painter = it, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryButtonEnabledPreview() = PrimaryButton(text = "Button text", onClick = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryButtonDisabledPreview() = PrimaryButton(text = "Button text", onClick = {}, enabled = false)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryButtonEnabledWithIconPreview() = PrimaryButton(
    text = "Button text",
    onClick = {},
    icon = painterResource(Res.drawable.ic_arrow_right_alt_24dp),
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryButtonEnabledLoadingPreview() = PrimaryButton(text = "Button text", onClick = {}, loading = true)
