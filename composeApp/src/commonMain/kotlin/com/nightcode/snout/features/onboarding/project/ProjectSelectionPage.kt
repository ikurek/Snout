package com.nightcode.snout.features.onboarding.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.projects.projectEntries
import com.nightcode.snout.features.projects.toProjectListEntries
import com.nightcode.snout.shared.domain.model.organization.Organization
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.project_selection_screen_subtitle
import snout.composeapp.generated.resources.project_selection_screen_title
import kotlin.uuid.Uuid

/** Headline over the organization → project list; [bottomPadding] lets the list scroll under the navigation bar. */
@Composable
internal fun ProjectSelectionPage(
    state: ProjectSelectionUiState,
    bottomPadding: PaddingValues,
    onRetry: () -> Unit,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header()
        Box(modifier = Modifier.weight(1f)) {
            Content(
                state = state,
                contentPadding = bottomPadding,
                onRetry = onRetry,
                onProjectSelected = onProjectSelected,
            )
        }
    }
}

@Composable
private fun Content(
    state: ProjectSelectionUiState,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    val organizations = state.organizations
    val error = state.error
    when {
        organizations != null -> LoadedContent(
            organizations = organizations,
            contentPadding = contentPadding,
            onRetry = onRetry,
            onProjectSelected = onProjectSelected,
        )

        error != null -> ErrorPlaceholder(error = error, onRetry = onRetry, modifier = Modifier.fillMaxSize())

        else -> SkeletonRows()
    }
}

/** Headline and its intro under the onboarding progress, lined up with the login step's. */
@Composable
private fun Header() {
    Column(
        modifier = Modifier.padding(horizontal = Spacing.large).padding(top = Spacing.large, bottom = Spacing.medium),
    ) {
        Text(
            text = stringResource(Res.string.project_selection_screen_title),
            style = SnoutTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        VerticalSpacer(Spacing.small)
        Text(
            text = stringResource(Res.string.project_selection_screen_subtitle),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadedContent(
    organizations: List<Organization>,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    if (organizations.all { it.projects.isEmpty() }) {
        NothingFoundPlaceholder(onRetry = onRetry, modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn(contentPadding = contentPadding) {
            projectEntries(
                entries = organizations.toProjectListEntries(),
                selectedProject = null,
                onProjectSelected = onProjectSelected,
            )
        }
    }
}
