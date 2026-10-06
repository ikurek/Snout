package com.nightcode.snout.features.projects

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.OrganizationLogo
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.Project
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_check_24dp
import snout.composeapp.generated.resources.initial_project_screen_project_count

/** Organization header shared by project selection and the project switcher. */
@Composable
internal fun OrganizationRow(organization: Organization) {
    EntityRow(
        title = organization.name,
        titleStyle = SnoutTheme.typography.titleMedium,
        subtitle = stringResource(Res.string.initial_project_screen_project_count, organization.projects.size),
        leading = { OrganizationLogo(name = organization.name, logoUrl = organization.logoUrl, size = 48.dp) },
    )
}

/** Project entry under its organization; indented so the hierarchy reads at a glance. */
@Composable
internal fun ProjectRow(project: Project, onClick: () -> Unit, isSelected: Boolean = false) {
    EntityRow(
        title = project.name,
        onClick = onClick,
        modifier = Modifier.padding(start = 16.dp),
        leading = { AvatarBox(name = project.name, size = 32.dp) },
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
