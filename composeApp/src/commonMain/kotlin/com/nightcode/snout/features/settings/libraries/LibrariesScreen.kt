package com.nightcode.snout.features.settings.libraries

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.mikepenz.aboutlibraries.entity.Library
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.DetailLoadingRowCount
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.settings.libraries.components.author
import com.nightcode.snout.features.settings.libraries.components.shortName
import com.nightcode.snout.shared.common.testdata.TestData
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.libraries_screen_title
import snout.composeapp.generated.resources.libraries_unknown_license

/** Every open-source library the app ships with, by name, with its version, author and license. */
@Composable
internal fun LibrariesScreen(
    state: LibrariesUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenLibrary: (uniqueId: String) -> Unit,
) {
    PushedScreenScaffold(
        title = stringResource(Res.string.libraries_screen_title),
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        val libraries = state.libraries
        val modifier = Modifier.padding(innerPadding.withoutBottom())
        if (libraries == null) {
            LoadingOrErrorContent(
                error = state.error,
                onRetry = onRefresh,
                modifier = modifier,
                loadingContent = { SkeletonRows(count = DetailLoadingRowCount) },
            )
        } else {
            RefreshableContent(
                isRefreshing = false,
                refreshError = null,
                onRefresh = onRefresh,
                modifier = modifier,
                isPullToRefreshEnabled = false,
            ) {
                LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + innerPadding.bottomOnly()) {
                    itemsIndexed(libraries, key = { _, library -> library.uniqueId }) { index, library ->
                        LibraryRow(
                            library = library,
                            divider = index < libraries.lastIndex,
                            onClick = { onOpenLibrary(library.uniqueId) },
                        )
                    }
                }
            }
        }
    }
}

/** Name over its Maven coordinates (Koin's artifacts share one name), then license, version and author. */
@Composable
private fun LibraryRow(library: Library, divider: Boolean, onClick: () -> Unit) {
    EntityRow(
        title = library.name,
        subtitle = library.uniqueId,
        subtitleStyle = SnoutTheme.typography.bodySmall,
        divider = divider,
        onClick = onClick,
        meta = {
            if (library.licenses.isEmpty()) {
                Tag(text = stringResource(Res.string.libraries_unknown_license))
            }
            library.licenses.forEach { Tag(text = it.shortName) }
            listOfNotNull(library.artifactVersion, library.author)
                .takeIf { it.isNotEmpty() }
                ?.let { MetaText(it.joinToString(" · ")) }
        },
    )
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LibrariesScreenPreview() = LibrariesScreen(
    state = LibrariesUiState(libraries = TestLibraries),
    onNavigateBack = {},
    onRefresh = {},
    onOpenLibrary = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LibrariesScreenLoadingPreview() = LibrariesScreen(
    state = LibrariesUiState(),
    onNavigateBack = {},
    onRefresh = {},
    onOpenLibrary = {},
)
