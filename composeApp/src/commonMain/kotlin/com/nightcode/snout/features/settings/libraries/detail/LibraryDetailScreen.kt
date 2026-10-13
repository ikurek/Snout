package com.nightcode.snout.features.settings.libraries.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.dataOrNull
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.organisms.LoadStateContainer
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.settings.libraries.TestLibrary
import com.nightcode.snout.features.settings.libraries.TestMitLibrary
import com.nightcode.snout.features.settings.libraries.components.author
import com.nightcode.snout.features.settings.libraries.components.homepage
import com.nightcode.snout.shared.common.testdata.TestData
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_open_in_new_24dp
import snout.composeapp.generated.resources.library_artifact_label
import snout.composeapp.generated.resources.library_author_label
import snout.composeapp.generated.resources.library_details_section
import snout.composeapp.generated.resources.library_license_online_label
import snout.composeapp.generated.resources.library_open_in_browser_content_description
import snout.composeapp.generated.resources.library_title_placeholder
import snout.composeapp.generated.resources.library_version_label
import snout.composeapp.generated.resources.library_website_label

/** One library: what it is, who makes it, a link to its website, and the full text of each of its licenses. */
@Composable
internal fun LibraryDetailScreen(
    state: LoadState<Library>,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenWebsite: (url: String) -> Unit,
) {
    PushedScreenScaffold(
        title = state.dataOrNull?.name ?: stringResource(Res.string.library_title_placeholder),
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        LoadStateContainer(
            state = state,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding.withoutBottom()),
        ) { library ->
            LibraryContent(library, onOpenWebsite, Modifier.fillMaxSize(), innerPadding.bottomOnly())
        }
    }
}

@Composable
private fun LibraryContent(
    library: Library,
    onOpenWebsite: (url: String) -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = Spacing.xxLarge),
    ) {
        library.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.large).padding(top = Spacing.small),
            )
        }
        SectionLabel(stringResource(Res.string.library_details_section))
        KeyValueRow(
            label = stringResource(Res.string.library_artifact_label),
            value = library.uniqueId,
            valueIsCode = true,
        )
        library.artifactVersion?.let {
            KeyValueRow(label = stringResource(Res.string.library_version_label), value = it, valueIsCode = true)
        }
        library.author?.let { KeyValueRow(label = stringResource(Res.string.library_author_label), value = it) }
        library.homepage?.let { url ->
            LinkRow(label = stringResource(Res.string.library_website_label), url = url, onOpen = onOpenWebsite)
        }
        library.licenses.forEach { LicenseSection(it, onOpenWebsite) }
    }
}

/** The license's full text as the build bundled it; only a license without one links out to its text instead. */
@Composable
private fun LicenseSection(license: License, onOpenWebsite: (url: String) -> Unit) {
    SectionLabel(license.name)
    val content = license.licenseContent?.takeIf { it.isNotBlank() }
    val url = license.url?.takeIf { it.isNotBlank() }
    when {
        content != null -> Text(
            text = content,
            style = SnoutTheme.typography.bodySmall,
            color = SnoutTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.large),
        )

        url != null ->
            LinkRow(label = stringResource(Res.string.library_license_online_label), url = url, onOpen = onOpenWebsite)
    }
}

/** A URL with the open in browser button, like the instance row in Settings. */
@Composable
private fun LinkRow(label: String, url: String, onOpen: (url: String) -> Unit) {
    KeyValueRow(
        label = label,
        value = url,
        valueMaxLines = 1,
        trailing = {
            IconButton(onClick = { onOpen(url) }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_open_in_new_24dp),
                    contentDescription = stringResource(Res.string.library_open_in_browser_content_description),
                    tint = SnoutTheme.colors.primary,
                )
            }
        },
    )
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LibraryDetailScreenPreview() = LibraryDetailScreen(
    state = LoadState.Loaded(TestMitLibrary),
    onNavigateBack = {},
    onRefresh = {},
    onOpenWebsite = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LibraryDetailScreenLicenseLinkPreview() = LibraryDetailScreen(
    state = LoadState.Loaded(
        TestLibrary.copy(licenses = TestLibrary.licenses.map { it.copy(licenseContent = null) }.toSet()),
    ),
    onNavigateBack = {},
    onRefresh = {},
    onOpenWebsite = {},
)
