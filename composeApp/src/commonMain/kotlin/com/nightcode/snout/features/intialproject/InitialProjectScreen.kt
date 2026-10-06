package com.nightcode.snout.features.intialproject

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.organisms.GenericErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.organisms.PermissionDeniedPlaceholder
import com.nightcode.snout.designsystem.organisms.RateLimitedPlaceholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.projects.projectEntries
import com.nightcode.snout.features.projects.toProjectListEntries
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.initial_project_screen_subtitle
import snout.composeapp.generated.resources.initial_project_screen_title
import kotlin.uuid.Uuid

@Composable
internal fun InitialProjectScreen(
    state: InitialProjectUiState,
    onRetry: () -> Unit,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { Header() },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when (state) {
                InitialProjectUiState.Loading -> SkeletonRows()

                is InitialProjectUiState.Error -> ErrorContent(error = state.error, onRetry = onRetry)

                is InitialProjectUiState.Loaded -> LoadedContent(
                    organizations = state.organizations,
                    onRetry = onRetry,
                    onProjectSelected = onProjectSelected,
                )
            }
        }
    }
}

/** No app bar on this screen: the headline starts 16dp below the status bar with its intro underneath. */
@Composable
private fun Header() {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
        Text(
            text = stringResource(Res.string.initial_project_screen_title),
            style = SnoutTheme.typography.headlineSmall,
        )
        VerticalSpacer(4.dp)
        Text(
            text = stringResource(Res.string.initial_project_screen_subtitle),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorContent(error: GetOrganizationsError, onRetry: () -> Unit) {
    when (error) {
        GetOrganizationsError.Forbidden ->
            PermissionDeniedPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())

        GetOrganizationsError.RateLimited ->
            RateLimitedPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())

        GetOrganizationsError.ConnectionError,
        GetOrganizationsError.UnexpectedError,
        ->
            GenericErrorPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun LoadedContent(
    organizations: List<Organization>,
    onRetry: () -> Unit,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    if (organizations.all { it.projects.isEmpty() }) {
        NothingFoundPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn {
            projectEntries(
                entries = organizations.toProjectListEntries(),
                selectedProject = null,
                onProjectSelected = onProjectSelected,
            )
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InitialProjectScreenLoadedPreview() = InitialProjectScreen(
    state = TestInitialProjectUiStateLoaded,
    onRetry = {},
    onProjectSelected = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InitialProjectScreenLoadingPreview() = InitialProjectScreen(
    state = InitialProjectUiState.Loading,
    onRetry = {},
    onProjectSelected = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InitialProjectScreenErrorPreview() = InitialProjectScreen(
    state = InitialProjectUiState.Error(GetOrganizationsError.UnexpectedError),
    onRetry = {},
    onProjectSelected = { _, _ -> },
)
