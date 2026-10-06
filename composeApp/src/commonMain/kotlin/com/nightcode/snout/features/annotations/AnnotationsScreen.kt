package com.nightcode.snout.features.annotations

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
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
import kotlin.time.Instant

private val DateColumnWidth = 78.dp

/** Section body with its own FAB and snackbar; the home shell or the section route supplies the app bar. */
@Composable
internal fun AnnotationsScreen(
    state: AnnotationsUiState,
    snackbarHostState: SnackbarHostState,
    showCreateSheet: Boolean,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onCreateDismiss: () -> Unit,
    onCreate: (content: String, dateMarker: Instant) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { AddAnnotationButton(onClick = onAddClick) },
    ) { innerPadding ->
        CachedStateContainer(
            state = state.annotations,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) { annotations ->
            LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                itemsIndexed(annotations, key = { _, annotation -> annotation.id }) { index, annotation ->
                    AnnotationRow(annotation = annotation, divider = index < annotations.lastIndex)
                }
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

/** Tonal FAB on `primaryContainer`; elevation is tonal in this design, so no shadow. */
@Composable
private fun AddAnnotationButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = SnoutTheme.shapes.large,
        containerColor = SnoutTheme.colors.primaryContainer,
        contentColor = SnoutTheme.colors.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add_24dp),
            contentDescription = stringResource(Res.string.add_annotation_content_description),
        )
    }
}

/** Date column on the left, then the note, its scope tag and who added it. */
@Composable
private fun AnnotationRow(annotation: Annotation, divider: Boolean) {
    EntityRow(
        title = annotation.content,
        titleMaxLines = 3,
        alignment = Alignment.Top,
        divider = divider,
        leading = {
            Text(
                text = annotation.dateMarker.formatShortDate(),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.width(DateColumnWidth),
            )
        },
        meta = {
            annotation.scope.label()?.let { Tag(text = annotation.targetName ?: it) }
        },
        supporting = {
            val meta = listOfNotNull(
                annotation.createdBy?.let { stringResource(Res.string.annotation_by, it) },
                annotation.createdAt?.formatRelative(),
            ).joinToString(" · ")
            if (meta.isNotEmpty()) MetaText(meta, modifier = Modifier.padding(top = 4.dp))
        },
    )
}

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
    onRefresh = {},
    onAddClick = {},
    onCreateDismiss = {},
    onCreate = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnnotationsScreenEmptyPreview() = AnnotationsScreen(
    state = AnnotationsUiState(annotations = CachedUiState.Empty(isRefreshing = false)),
    snackbarHostState = remember { SnackbarHostState() },
    showCreateSheet = false,
    onRefresh = {},
    onAddClick = {},
    onCreateDismiss = {},
    onCreate = { _, _ -> },
)
