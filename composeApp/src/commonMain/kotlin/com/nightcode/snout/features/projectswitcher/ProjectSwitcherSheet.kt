package com.nightcode.snout.features.projectswitcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.organisms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.projects.projectEntries
import com.nightcode.snout.features.projects.toProjectListEntries
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.organization.Organization
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.switch_project_bottom_sheet_nothing_found_title
import snout.composeapp.generated.resources.switch_project_bottom_sheet_title
import kotlin.uuid.Uuid

private const val LoadingRowCount = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectSwitcherSheet(
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
        ProjectSwitcherContent(
            state = state,
            onProjectSelected = { orgId, projectId ->
                onDismiss()
                onProjectSelected(orgId, projectId)
            },
        )
    }
}

@Composable
private fun ProjectSwitcherContent(
    state: ProjectSwitcherUiState,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    Text(
        text = stringResource(Res.string.switch_project_bottom_sheet_title),
        style = SnoutTheme.typography.titleLarge,
        modifier = Modifier
            .padding(horizontal = Spacing.large)
            .padding(top = Spacing.small, bottom = Spacing.medium),
    )
    when (state) {
        ProjectSwitcherUiState.Loading -> SkeletonRows(count = LoadingRowCount)

        is ProjectSwitcherUiState.NotFound -> NotFoundContent()

        is ProjectSwitcherUiState.Loaded -> LoadedContent(
            organizations = state.organizations,
            selectedProject = state.selectedProject,
            onProjectSelected = onProjectSelected,
        )
    }
}

@Composable
private fun NotFoundContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.huge),
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
    selectedProject: SelectedProject,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large)) {
        projectEntries(
            entries = organizations.toProjectListEntries(),
            selectedProject = selectedProject,
            onProjectSelected = onProjectSelected,
        )
    }
}

/** The sheet's content on its `surfaceContainer` ground; the modal itself does not render in a static preview. */
@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ProjectSwitcherSheetPreview() = Column(modifier = Modifier.background(SnoutTheme.colors.surfaceContainer)) {
    ProjectSwitcherContent(state = TestProjectSwitcherUiStateLoaded, onProjectSelected = { _, _ -> })
}
