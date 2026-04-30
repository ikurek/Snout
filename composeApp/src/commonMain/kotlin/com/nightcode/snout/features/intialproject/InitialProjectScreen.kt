@file:OptIn(ExperimentalUuidApi::class)

package com.nightcode.snout.features.intialproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.HorizontalSpacer
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.molecules.OrganizationLogo
import com.nightcode.snout.designsystem.organisms.GenericErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.organisms.PermissionDeniedPlaceholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.Project
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.initial_project_screen_project_count
import snout.composeapp.generated.resources.initial_project_screen_subtitle
import snout.composeapp.generated.resources.initial_project_screen_title
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InitialProjectScreen(
    state: InitialProjectUiState,
    onRetry: () -> Unit,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { TopBar() },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when (state) {
                InitialProjectUiState.Loading -> LoadingContent()

                is InitialProjectUiState.Error -> ErrorContent(
                    error = state.error,
                    onRetry = onRetry,
                )

                is InitialProjectUiState.Loaded -> LoadedContent(
                    organizations = state.organizations,
                    onRetry = onRetry,
                    onProjectSelected = onProjectSelected,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Text(stringResource(Res.string.initial_project_screen_title)) },
        subtitle = { Text(stringResource(Res.string.initial_project_screen_subtitle)) },
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(error: GetOrganizationsError, onRetry: () -> Unit) {
    when (error) {
        GetOrganizationsError.AuthorizationFailed ->
            PermissionDeniedPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())

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
    if (organizations.isEmpty() || organizations.all { it.projects.isEmpty() }) {
        NothingFoundPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())
        return
    } else {
        LazyColumn(contentPadding = PaddingValues(vertical = 0.dp)) {
            organizations.forEach { org ->
                item(key = "org_${org.id}") {
                    OrgHeader(org = org)
                }
                items(items = org.projects, key = { "project_${it.id}" }) { project ->
                    ProjectItem(
                        project = project,
                        onClick = { onProjectSelected(org.id, project.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OrgHeader(org: Organization) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OrganizationLogo(
            name = org.name,
            logoUrl = org.logoUrl,
            size = 48.dp,
        )
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = org.name,
                style = SnoutTheme.typography.titleMedium,
            )
            VerticalSpacer(4.dp)
            Text(
                text = stringResource(
                    Res.string.initial_project_screen_project_count,
                    org.projects.size,
                ),
                style = SnoutTheme.typography.labelMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProjectItem(project: Project, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 32.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarBox(name = project.name, size = 32.dp)
        HorizontalSpacer(16.dp)
        Text(
            text = project.name,
            style = SnoutTheme.typography.titleSmall,
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun InitialProjectScreenLoadedPreview() = InitialProjectScreen(
    state = InitialProjectUiState.Loaded(
        organizations = listOf(
            Organization(
                id = Uuid.random(),
                name = "Nightcode",
                slug = "nightcode",
                logoUrl = null,
                projects = listOf(
                    Project(id = 1, name = "Production"),
                    Project(id = 2, name = "Staging"),
                ),
            ),
            Organization(
                id = Uuid.random(),
                name = "Personal",
                slug = "personal",
                logoUrl = null,
                projects = listOf(
                    Project(id = 3, name = "My App"),
                ),
            ),
        ),
    ),
    onRetry = {},
    onProjectSelected = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun InitialProjectScreenLoadingPreview() = InitialProjectScreen(
    state = InitialProjectUiState.Loading,
    onRetry = {},
    onProjectSelected = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun InitialProjectScreenErrorPreview() = InitialProjectScreen(
    state = InitialProjectUiState.Error(GetOrganizationsError.UnexpectedError),
    onRetry = {},
    onProjectSelected = { _, _ -> },
)
