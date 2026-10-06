package com.nightcode.snout.features.annotations

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.LabelChip
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.annotations.AnnotationScope
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.add_annotation_content_description
import snout.composeapp.generated.resources.annotation_by
import snout.composeapp.generated.resources.annotation_scope_dashboard
import snout.composeapp.generated.resources.annotation_scope_insight
import snout.composeapp.generated.resources.annotation_scope_organization
import snout.composeapp.generated.resources.annotation_scope_project
import snout.composeapp.generated.resources.ic_add_24dp
import snout.composeapp.generated.resources.more_annotations_title
import kotlin.time.Instant

@Composable
internal fun AnnotationsScreen(
    state: AnnotationsUiState,
    snackbarHostState: SnackbarHostState,
    showCreateSheet: Boolean,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onCreateDismiss: () -> Unit,
    onCreate: (content: String, dateMarker: Instant) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    ListScreenScaffold(
        title = stringResource(Res.string.more_annotations_title),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, shape = SnoutTheme.shapes.large) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24dp),
                    contentDescription = stringResource(Res.string.add_annotation_content_description),
                )
            }
        },
    ) { innerPadding ->
        CachedStateContainer(
            state = state.annotations,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) { annotations ->
            LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)) {
                items(annotations, key = { it.id }) { AnnotationRow(it) }
            }
        }
    }

    CreateAnnotationSheet(
        isVisible = showCreateSheet,
        status = state.create,
        onDismiss = onCreateDismiss,
        onCreate = onCreate,
    )
}

@Composable
private fun AnnotationRow(annotation: Annotation) {
    EntityRow(
        title = annotation.content,
        titleStyle = SnoutTheme.typography.bodyMedium,
        titleMaxLines = 4,
        alignment = Alignment.Top,
        leading = {
            Text(
                text = annotation.dateMarker.formatShortDate(),
                style = SnoutTheme.code.small,
                color = SnoutTheme.colors.primary,
                modifier = Modifier.width(DateColumnWidth),
            )
        },
        meta = {
            annotation.scope.label()?.let { LabelChip(text = annotation.targetName ?: it) }
            val meta = listOfNotNull(
                annotation.createdBy?.let { stringResource(Res.string.annotation_by, it) },
                annotation.createdAt?.formatRelative(),
            ).joinToString(" · ")
            if (meta.isNotEmpty()) MetaText(meta)
        },
    )
}

private val DateColumnWidth = 88.dp

@Composable
private fun AnnotationScope.label(): String? = when (this) {
    AnnotationScope.Project -> stringResource(Res.string.annotation_scope_project)
    AnnotationScope.Organization -> stringResource(Res.string.annotation_scope_organization)
    AnnotationScope.Dashboard -> stringResource(Res.string.annotation_scope_dashboard)
    AnnotationScope.Insight -> stringResource(Res.string.annotation_scope_insight)
    AnnotationScope.Unknown -> null
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnnotationsScreenPreview() = AnnotationsScreen(
    state = TestAnnotationsUiState,
    snackbarHostState = remember { SnackbarHostState() },
    showCreateSheet = false,
    onNavigateBack = {},
    onRefresh = {},
    onAddClick = {},
    onCreateDismiss = {},
    onCreate = { _, _ -> },
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnnotationsScreenEmptyPreview() = AnnotationsScreen(
    state = AnnotationsUiState(annotations = CachedUiState.Empty(isRefreshing = false)),
    snackbarHostState = remember { SnackbarHostState() },
    showCreateSheet = false,
    onNavigateBack = {},
    onRefresh = {},
    onAddClick = {},
    onCreateDismiss = {},
    onCreate = { _, _ -> },
    onOpenInPostHog = {},
)
