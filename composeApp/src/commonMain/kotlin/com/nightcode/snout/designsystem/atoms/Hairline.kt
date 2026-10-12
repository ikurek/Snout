package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing

/**
 * The 1dp `outlineVariant` rule between list rows and table header and body. [inset] keeps it clear of the 16dp
 * screen edges, in line with the row content.
 */
@Composable
internal fun Hairline(modifier: Modifier = Modifier, inset: Boolean = false) {
    HorizontalDivider(
        color = SnoutTheme.colors.outlineVariant,
        modifier = if (inset) modifier.padding(horizontal = Spacing.large) else modifier,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun HairlinePreview() = Column(modifier = Modifier.padding(vertical = Spacing.large)) {
    Hairline()
    VerticalSpacer(Spacing.large)
    Hairline(inset = true)
}
