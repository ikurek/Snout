package com.nightcode.snout.features.settings.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.sections.description
import com.nightcode.snout.common.sections.icon
import com.nightcode.snout.common.sections.label
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButton
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.organisms.OptionPickerSheet
import com.nightcode.snout.designsystem.organisms.PickerOption
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.settings_navigation_landing_label
import snout.composeapp.generated.resources.settings_navigation_landing_section
import snout.composeapp.generated.resources.settings_navigation_more_note
import snout.composeapp.generated.resources.settings_navigation_reset
import snout.composeapp.generated.resources.settings_navigation_slot
import snout.composeapp.generated.resources.settings_navigation_tabs_section
import snout.composeapp.generated.resources.settings_navigation_title

private const val LoadingRowCount = 4

/** Which picker is open: a bar slot by index, or the landing tab. Saved as a plain Int so it survives rotation. */
private const val LandingPicker = -1

@Composable
internal fun NavigationSettingsScreen(
    preferences: NavigationPreferences?,
    onNavigateBack: () -> Unit,
    onSlotSelected: (slot: Int, section: HomeSection) -> Unit,
    onLandingSelected: (HomeSection) -> Unit,
    onReset: () -> Unit,
) {
    var openPicker by rememberSaveable { mutableStateOf<Int?>(null) }

    PushedScreenScaffold(
        title = stringResource(Res.string.settings_navigation_title),
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())
        if (preferences == null) {
            SkeletonRows(modifier = modifier, count = LoadingRowCount)
        } else {
            NavigationContent(
                preferences = preferences,
                modifier = modifier,
                contentPadding = innerPadding.bottomOnly(),
                onOpenSlotPicker = { openPicker = it },
                onOpenLandingPicker = { openPicker = LandingPicker },
                onReset = onReset,
            )
        }
    }

    if (preferences != null) {
        val picker = openPicker
        OptionPickerSheet(
            isVisible = picker != null,
            title = when {
                picker == null -> ""
                picker == LandingPicker -> stringResource(Res.string.settings_navigation_landing_label)
                else -> stringResource(Res.string.settings_navigation_slot, picker + 1)
            },
            options = (if (picker == LandingPicker) preferences.tabs else HomeSection.entries).map { section ->
                PickerOption(
                    value = section,
                    title = stringResource(section.label),
                    subtitle = stringResource(section.description),
                )
            },
            selected = when {
                picker == null -> null
                picker == LandingPicker -> preferences.landing
                else -> preferences.tabs[picker]
            },
            onSelect = { section ->
                when (picker) {
                    null -> Unit
                    LandingPicker -> onLandingSelected(section)
                    else -> onSlotSelected(picker, section)
                }
            },
            onDismiss = { openPicker = null },
            leading = { SectionIcon(it) },
        )
    }
}

@Composable
private fun NavigationContent(
    preferences: NavigationPreferences,
    modifier: Modifier,
    contentPadding: PaddingValues,
    onOpenSlotPicker: (slot: Int) -> Unit,
    onOpenLandingPicker: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = Spacing.xxLarge),
    ) {
        SectionLabel(stringResource(Res.string.settings_navigation_tabs_section))
        preferences.tabs.forEachIndexed { index, section ->
            KeyValueRow(
                label = stringResource(Res.string.settings_navigation_slot, index + 1),
                value = stringResource(section.label),
                leadingIcon = painterResource(section.icon),
                onClick = { onOpenSlotPicker(index) },
            )
        }
        VerticalSpacer(Spacing.medium)
        InfoBanner(
            text = stringResource(Res.string.settings_navigation_more_note),
            modifier = Modifier.padding(horizontal = Spacing.large),
        )

        SectionLabel(stringResource(Res.string.settings_navigation_landing_section))
        KeyValueRow(
            label = stringResource(Res.string.settings_navigation_landing_label),
            value = stringResource(preferences.landing.label),
            leadingIcon = painterResource(preferences.landing.icon),
            onClick = onOpenLandingPicker,
        )

        VerticalSpacer(Spacing.xxLarge)
        SecondaryButton(
            text = stringResource(Res.string.settings_navigation_reset),
            onClick = onReset,
            enabled = preferences != NavigationPreferences.Default,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
        )
    }
}

@Composable
private fun SectionIcon(section: HomeSection) {
    Icon(
        painter = painterResource(section.icon),
        contentDescription = null,
        tint = SnoutTheme.colors.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun NavigationSettingsScreenPreview() = NavigationSettingsScreen(
    preferences = NavigationPreferences.Default.withTab(3, HomeSection.Experiments),
    onNavigateBack = {},
    onSlotSelected = { _, _ -> },
    onLandingSelected = {},
    onReset = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun NavigationSettingsScreenLoadingPreview() = NavigationSettingsScreen(
    preferences = null,
    onNavigateBack = {},
    onSlotSelected = { _, _ -> },
    onLandingSelected = {},
    onReset = {},
)
