package com.nightcode.snout.features.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.home.description
import com.nightcode.snout.features.home.icon
import com.nightcode.snout.features.home.label
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_chevron_right_24dp

/** Every section that did not get a bottom-bar slot: 20dp leading icon, title, description, trailing chevron. */
@Composable
internal fun MoreScreen(sections: List<HomeSection>, onItemClick: (HomeSection) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        sections.forEachIndexed { index, section ->
            EntityRow(
                title = stringResource(section.label),
                subtitle = stringResource(section.description),
                divider = index < sections.lastIndex,
                onClick = { onItemClick(section) },
                leading = {
                    Icon(
                        painter = painterResource(section.icon),
                        contentDescription = null,
                        tint = SnoutTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailing = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right_24dp),
                        contentDescription = null,
                        tint = SnoutTheme.colors.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun MoreScreenPreview() = MoreScreen(sections = NavigationPreferences.Default.hidden, onItemClick = {})
