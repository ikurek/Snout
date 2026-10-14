package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_info_outlined_24dp

/** Inline, non-dismissable note about a limitation or context ("Values can only be edited in PostHog"). Tonal. */
@Composable
internal fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.secondaryContainer,
        contentColor = SnoutTheme.colors.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_info_outlined_24dp),
                contentDescription = null,
                modifier = Modifier.size(BannerIconSize),
            )
            Text(text = text, style = SnoutTheme.typography.bodyMedium)
        }
    }
}

internal val BannerIconSize = 18.dp

/** Trailing text action inside a banner, drawn in the banner's content colour. */
@Composable
internal fun BannerAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = SnoutTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.xxSmall, vertical = Spacing.xxxSmall),
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InfoBannerPreview() = InfoBanner(text = "Values can only be edited in PostHog. Open the flag there to change it.")
