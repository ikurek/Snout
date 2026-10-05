package com.nightcode.snout.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.HorizontalSpacer
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.molecules.OrganizationLogo
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.Project
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_check_24dp
import snout.composeapp.generated.resources.initial_project_screen_project_count
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
            item(key = "org_${org.id}") {
                OrgHeader(org = org)
            }
            items(items = org.projects, key = { "project_${it.id}" }) { project ->
                val isSelected = selectedProject?.organizationId == org.id &&
                    selectedProject.projectId == project.id
                ProjectSwitcherItem(
                    project = project,
                    isSelected = isSelected,
                    onClick = { onProjectSelected(org.id, project.id) },
                )
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
        OrganizationLogo(name = org.name, logoUrl = org.logoUrl, size = 48.dp)
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = org.name, style = SnoutTheme.typography.titleMedium)
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
private fun ProjectSwitcherItem(project: Project, isSelected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_24dp),
                contentDescription = null,
                tint = SnoutTheme.colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
