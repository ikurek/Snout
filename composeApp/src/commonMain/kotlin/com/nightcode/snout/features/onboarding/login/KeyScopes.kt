package com.nightcode.snout.features.onboarding.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.Hairline
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.molecules.CodeChip
import com.nightcode.snout.designsystem.molecules.TonalCard
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_chevron_right_24dp
import snout.composeapp.generated.resources.login_scopes_account
import snout.composeapp.generated.resources.login_scopes_activity_log
import snout.composeapp.generated.resources.login_scopes_add_annotations
import snout.composeapp.generated.resources.login_scopes_analytics
import snout.composeapp.generated.resources.login_scopes_annotations
import snout.composeapp.generated.resources.login_scopes_collapsed
import snout.composeapp.generated.resources.login_scopes_dashboards
import snout.composeapp.generated.resources.login_scopes_errors
import snout.composeapp.generated.resources.login_scopes_expanded
import snout.composeapp.generated.resources.login_scopes_experiments
import snout.composeapp.generated.resources.login_scopes_favorite_insights
import snout.composeapp.generated.resources.login_scopes_feature_flags
import snout.composeapp.generated.resources.login_scopes_footnote
import snout.composeapp.generated.resources.login_scopes_group_changes
import snout.composeapp.generated.resources.login_scopes_group_sections
import snout.composeapp.generated.resources.login_scopes_group_sign_in
import snout.composeapp.generated.resources.login_scopes_insights
import snout.composeapp.generated.resources.login_scopes_logs
import snout.composeapp.generated.resources.login_scopes_resolve_errors
import snout.composeapp.generated.resources.login_scopes_toggle_flags
import snout.composeapp.generated.resources.login_screen_scopes_toggle

/** An app feature and the personal API key scopes it calls the API with. Scope names stay untranslated. */
private class ScopedFeature(val name: StringResource, val scopes: List<String>)

private class ScopeGroup(val title: StringResource, val features: List<ScopedFeature>)

/** Mirrors the endpoints each feature calls; keep in step with the README's scope list. */
private val ScopeGroups = listOf(
    ScopeGroup(
        Res.string.login_scopes_group_sign_in,
        listOf(
            ScopedFeature(Res.string.login_scopes_account, listOf("user:read", "organization:read", "project:read")),
        ),
    ),
    ScopeGroup(
        Res.string.login_scopes_group_sections,
        listOf(
            ScopedFeature(Res.string.login_scopes_analytics, listOf("query:read")),
            ScopedFeature(Res.string.login_scopes_dashboards, listOf("dashboard:read", "insight:read", "query:read")),
            ScopedFeature(Res.string.login_scopes_insights, listOf("insight:read", "query:read")),
            ScopedFeature(Res.string.login_scopes_feature_flags, listOf("feature_flag:read")),
            ScopedFeature(Res.string.login_scopes_experiments, listOf("experiment:read")),
            ScopedFeature(Res.string.login_scopes_errors, listOf("error_tracking:read", "query:read")),
            ScopedFeature(Res.string.login_scopes_logs, listOf("logs:read")),
            ScopedFeature(Res.string.login_scopes_annotations, listOf("annotation:read")),
            ScopedFeature(Res.string.login_scopes_activity_log, listOf("activity_log:read")),
        ),
    ),
    ScopeGroup(
        Res.string.login_scopes_group_changes,
        listOf(
            ScopedFeature(Res.string.login_scopes_toggle_flags, listOf("feature_flag:write")),
            ScopedFeature(Res.string.login_scopes_resolve_errors, listOf("error_tracking:write")),
            ScopedFeature(Res.string.login_scopes_add_annotations, listOf("annotation:write")),
            ScopedFeature(Res.string.login_scopes_favorite_insights, listOf("insight:write")),
        ),
    ),
)

private const val ChevronCollapsedRotation = 90f
private const val ChevronExpandedRotation = 270f

/**
 * A question that unfolds into which scope each part of the app needs, grouped into signing in, reading each
 * section and making changes.
 */
@Composable
internal fun KeyScopes(modifier: Modifier = Modifier, initiallyExpanded: Boolean = false) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(if (expanded) ChevronExpandedRotation else ChevronCollapsedRotation)
    val stateText = stringResource(
        if (expanded) Res.string.login_scopes_expanded else Res.string.login_scopes_collapsed,
    )
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { expanded = expanded.not() }
                .semantics { stateDescription = stateText }
                .padding(vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
        ) {
            Text(
                text = stringResource(Res.string.login_screen_scopes_toggle),
                modifier = Modifier.weight(1f),
                style = SnoutTheme.typography.labelLarge,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right_24dp),
                contentDescription = null,
                modifier = Modifier.size(20.dp).rotate(chevronRotation),
                tint = SnoutTheme.colors.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = expanded) {
            ScopesCard(modifier = Modifier.padding(top = Spacing.xxSmall))
        }
    }
}

/** Tonal card of grouped feature rows, with a closing note that missing scopes only lock their own section. */
@Composable
private fun ScopesCard(modifier: Modifier = Modifier) {
    TonalCard(modifier = modifier, contentPadding = PaddingValues(bottom = Spacing.large)) {
        // One child, so the card's spacing doesn't open gaps between the rows.
        Column {
            ScopeGroups.forEach { group ->
                SectionLabel(text = stringResource(group.title))
                group.features.forEachIndexed { index, feature ->
                    FeatureScopesRow(feature = feature)
                    if (index < group.features.lastIndex) {
                        Hairline(inset = true)
                    }
                }
            }
            Text(
                text = stringResource(Res.string.login_scopes_footnote),
                modifier = Modifier.padding(start = Spacing.large, end = Spacing.large, top = Spacing.large),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** Feature on the left, its scopes as code chips wrapping on the right; read aloud as one sentence. */
@Composable
private fun FeatureScopesRow(feature: ScopedFeature) {
    val name = stringResource(feature.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$name: ${feature.scopes.joinToString()}" }
            .padding(horizontal = Spacing.large, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Text(text = name, modifier = Modifier.weight(1f), style = SnoutTheme.typography.bodyMedium)
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxSmall, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
        ) {
            feature.scopes.forEach { CodeChip(text = it) }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun KeyScopesPreview() = KeyScopes(modifier = Modifier.padding(Spacing.large), initiallyExpanded = true)
