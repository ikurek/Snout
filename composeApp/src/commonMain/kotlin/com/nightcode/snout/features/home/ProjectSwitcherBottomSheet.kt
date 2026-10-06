package com.nightcode.snout.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.projects.OrganizationRow
import com.nightcode.snout.features.projects.ProjectRow
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.organization.Organization
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.switch_project_bottom_sheet_nothing_found_title
import snout.composeapp.generated.resources.switch_project_bottom_sheet_title
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectSwitcherBottomSheet(
    isVisible: Boolean,
    state: ProjectSwitcherUiState,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    AnimatedBottomSheet(
        isVisible = isVisible,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Text(
            text = stringResource(Res.string.switch_project_bottom_sheet_title),
            style = SnoutTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        when (state) {
            ProjectSwitcherUiState.Loading -> LoadingContent()

            is ProjectSwitcherUiState.NotFound -> NotFoundContent()

            is ProjectSwitcherUiState.Loaded -> LoadedContent(
                organizations = state.organizations,
                selectedProject = state.selectedProject,
                onProjectSelected = { orgId, projectId ->
                    onDismiss()
                    onProjectSelected(orgId, projectId)
                },
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NotFoundContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.switch_project_bottom_sheet_nothing_found_title),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadedContent(
    organizations: List<Organization>,
    selectedProject: SelectedProject?,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        organizations.forEach { org ->
            item(key = "org_${org.id}") { OrganizationRow(organization = org) }
            items(items = org.projects, key = { "project_${it.id}" }) { project ->
                val isSelected = selectedProject?.organizationId == org.id &&
                    selectedProject.projectId == project.id
                ProjectRow(
                    project = project,
                    isSelected = isSelected,
                    onClick = { onProjectSelected(org.id, project.id) },
                )
            }
        }
    }
}
