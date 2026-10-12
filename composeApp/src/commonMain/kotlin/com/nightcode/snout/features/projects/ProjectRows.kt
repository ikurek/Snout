package com.nightcode.snout.features.projects

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.OrganizationLogo
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.Project
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_check_24dp
import snout.composeapp.generated.resources.initial_project_screen_project_count
import kotlin.uuid.Uuid

internal fun List<Organization>.toProjectListEntries(): List<ProjectListEntry> = flatMap { organization ->
    listOf(ProjectListEntry.OrganizationHeader(organization)) +
        organization.projects.map { ProjectListEntry.ProjectItem(organization.id, it) }
}

/** Flattened organization → projects hierarchy so both the selection screen and the switcher render one list. */
internal sealed interface ProjectListEntry {
    val key: String

    data class OrganizationHeader(val organization: Organization) : ProjectListEntry {
        override val key: String get() = "org_${organization.id}"
    }

    data class ProjectItem(val organizationId: Uuid, val project: Project) : ProjectListEntry {
        override val key: String get() = "project_${project.id}"
    }
}

/** Organization headers with their projects indented beneath; the last row has no divider. */
internal fun LazyListScope.projectEntries(
    entries: List<ProjectListEntry>,
    selectedProject: SelectedProject?,
    onProjectSelected: (organizationId: Uuid, projectId: Long) -> Unit,
) {
    itemsIndexed(entries, key = { _, entry -> entry.key }) { index, entry ->
        val divider = index < entries.lastIndex
        when (entry) {
            is ProjectListEntry.OrganizationHeader -> OrganizationRow(
                organization = entry.organization,
                divider = divider,
            )

            is ProjectListEntry.ProjectItem -> ProjectRow(
                project = entry.project,
                isSelected = selectedProject?.organizationId == entry.organizationId &&
                    selectedProject.projectId == entry.project.id,
                divider = divider,
                onClick = { onProjectSelected(entry.organizationId, entry.project.id) },
            )
        }
    }
}

/** Organization header: logo or initials, name and the number of active projects. Not clickable, no chevron. */
@Composable
internal fun OrganizationRow(organization: Organization, divider: Boolean = true) {
    EntityRow(
        title = organization.name,
        subtitle = stringResource(Res.string.initial_project_screen_project_count, organization.projects.size),
        divider = divider,
        leading = { OrganizationLogo(name = organization.name, logoUrl = organization.logoUrl) },
    )
}

/** Project entry under its organization; indented 12dp so the hierarchy reads at a glance. */
@Composable
internal fun ProjectRow(project: Project, onClick: () -> Unit, isSelected: Boolean = false, divider: Boolean = true) {
    EntityRow(
        title = project.name,
        onClick = onClick,
        divider = divider,
        modifier = Modifier.padding(start = Spacing.medium),
        leading = { AvatarBox(name = project.name) },
        trailing = if (isSelected) {
            {
                Icon(
                    painter = painterResource(Res.drawable.ic_check_24dp),
                    contentDescription = null,
                    tint = SnoutTheme.colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            null
        },
    )
}
