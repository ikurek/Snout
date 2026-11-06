package com.nightcode.snout.features.errortracking.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatDateTime
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.designsystem.atoms.Hairline
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionOccurrence
import com.nightcode.snout.shared.domain.model.errortracking.OccurrenceException
import com.nightcode.snout.shared.domain.model.errortracking.TestExceptionOccurrences
import com.nightcode.snout.shared.domain.model.errortracking.TestOccurrenceExceptions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_exceptions_error_generic
import snout.composeapp.generated.resources.error_tracking_exceptions_error_gone
import snout.composeapp.generated.resources.error_tracking_occurrence_app_version
import snout.composeapp.generated.resources.error_tracking_occurrence_browser
import snout.composeapp.generated.resources.error_tracking_occurrence_captured
import snout.composeapp.generated.resources.error_tracking_occurrence_device
import snout.composeapp.generated.resources.error_tracking_occurrence_os
import snout.composeapp.generated.resources.error_tracking_occurrence_person
import snout.composeapp.generated.resources.error_tracking_occurrence_screen
import snout.composeapp.generated.resources.error_tracking_occurrence_section
import snout.composeapp.generated.resources.error_tracking_occurrence_url
import snout.composeapp.generated.resources.error_tracking_occurrences_empty
import snout.composeapp.generated.resources.error_tracking_occurrences_error_forbidden
import snout.composeapp.generated.resources.error_tracking_occurrences_error_generic
import snout.composeapp.generated.resources.error_tracking_recent_occurrences_section
import snout.composeapp.generated.resources.error_tracking_stack_trace_section
import snout.composeapp.generated.resources.ic_check_24dp

/** An occurrence PostHog no longer has can't be retried, so its banner reloads the issue, which picks the newest. */
@Composable
internal fun OccurrenceSection(
    occurrence: ExceptionOccurrence,
    exceptions: List<OccurrenceException>?,
    error: DataError?,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.error_tracking_occurrence_section))
        OccurrenceProperties(occurrence)
        SectionLabel(stringResource(Res.string.error_tracking_stack_trace_section))
        when {
            error == DataError.NotFound -> InlineErrorBanner(
                text = stringResource(Res.string.error_tracking_exceptions_error_gone),
                onAction = onRefresh,
                modifier = Modifier.padding(horizontal = Spacing.large),
            )

            error != null -> InlineErrorBanner(
                text = stringResource(
                    error.actionFailedMessage(
                        generic = Res.string.error_tracking_exceptions_error_generic,
                        forbidden = Res.string.error_tracking_occurrences_error_forbidden,
                    ),
                ),
                onAction = onRetry,
                modifier = Modifier.padding(horizontal = Spacing.large),
            )

            exceptions == null -> SkeletonRows(count = StackTraceLoadingRowCount)

            else -> exceptions.forEachIndexed { index, exception ->
                if (index > 0) Hairline(inset = true, modifier = Modifier.padding(vertical = Spacing.small))
                ExceptionStackTrace(exception)
            }
        }
    }
}

@Composable
internal fun RecentOccurrencesSection(
    occurrences: List<ExceptionOccurrence>?,
    error: DataError?,
    selectedOccurrenceId: String?,
    onRetry: () -> Unit,
    onOccurrenceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.error_tracking_recent_occurrences_section))
        when {
            occurrences == null && error != null -> InlineErrorBanner(
                text = stringResource(
                    error.actionFailedMessage(
                        generic = Res.string.error_tracking_occurrences_error_generic,
                        forbidden = Res.string.error_tracking_occurrences_error_forbidden,
                    ),
                ),
                onAction = onRetry,
                modifier = Modifier.padding(horizontal = Spacing.large),
            )

            occurrences == null -> SkeletonRows(count = OccurrencesLoadingRowCount)

            occurrences.isEmpty() -> Text(
                text = stringResource(Res.string.error_tracking_occurrences_empty),
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
            )

            else -> occurrences.forEachIndexed { index, occurrence ->
                OccurrenceRow(
                    occurrence = occurrence,
                    isSelected = occurrence.id == selectedOccurrenceId,
                    onClick = { onOccurrenceSelected(occurrence.id) },
                    divider = index < occurrences.lastIndex,
                )
            }
        }
    }
}

private const val StackTraceLoadingRowCount = 4
private const val OccurrencesLoadingRowCount = 3

@Composable
private fun OccurrenceProperties(occurrence: ExceptionOccurrence, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        KeyValueRow(
            label = stringResource(Res.string.error_tracking_occurrence_captured),
            value = occurrence.timestamp.formatDateTime(),
        )
        occurrence.person?.let {
            KeyValueRow(label = stringResource(Res.string.error_tracking_occurrence_person), value = it)
        }
        occurrence.os?.let {
            KeyValueRow(
                label = stringResource(Res.string.error_tracking_occurrence_os),
                value = listOfNotNull(it, occurrence.osVersion).joinToString(" "),
            )
        }
        occurrence.appVersion?.let {
            KeyValueRow(
                label = stringResource(Res.string.error_tracking_occurrence_app_version),
                value = it,
                valueIsCode = true,
            )
        }
        occurrence.browser?.let {
            KeyValueRow(
                label = stringResource(Res.string.error_tracking_occurrence_browser),
                value = listOfNotNull(it, occurrence.browserVersion).joinToString(" "),
            )
        }
        occurrence.deviceModel?.let {
            KeyValueRow(label = stringResource(Res.string.error_tracking_occurrence_device), value = it)
        }
        occurrence.url?.let {
            KeyValueRow(
                label = stringResource(Res.string.error_tracking_occurrence_url),
                value = it,
                valueIsCode = true,
                valueMaxLines = 3,
            )
        }
        occurrence.screen?.let {
            KeyValueRow(
                label = stringResource(Res.string.error_tracking_occurrence_screen),
                value = it,
                valueIsCode = true,
            )
        }
    }
}

@Composable
private fun OccurrenceRow(
    occurrence: ExceptionOccurrence,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    divider: Boolean = true,
) {
    EntityRow(
        title = occurrence.timestamp.formatRelative(),
        titleStyle = SnoutTheme.typography.bodyLarge,
        subtitle = occurrence.summary().takeIf { it.isNotEmpty() },
        subtitleStyle = SnoutTheme.typography.bodySmall,
        divider = divider,
        onClick = onClick,
        modifier = modifier,
        meta = (occurrence.url ?: occurrence.screen)?.let { place -> { MetaText(place) } },
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

private fun ExceptionOccurrence.summary(): String = listOfNotNull(
    os?.let { listOfNotNull(it, osVersion).joinToString(" ") },
    appVersion,
    browser,
    person,
).joinToString(" · ")

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OccurrenceSectionPreview() = OccurrenceSection(
    occurrence = TestExceptionOccurrences.first(),
    exceptions = TestOccurrenceExceptions,
    error = null,
    onRefresh = {},
    onRetry = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OccurrenceSectionLoadingPreview() = OccurrenceSection(
    occurrence = TestExceptionOccurrences.last(),
    exceptions = null,
    error = null,
    onRefresh = {},
    onRetry = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OccurrenceSectionGonePreview() = OccurrenceSection(
    occurrence = TestExceptionOccurrences.last(),
    exceptions = null,
    error = DataError.NotFound,
    onRefresh = {},
    onRetry = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun RecentOccurrencesSectionPreview() = RecentOccurrencesSection(
    occurrences = TestExceptionOccurrences,
    error = null,
    selectedOccurrenceId = TestExceptionOccurrences.first().id,
    onRetry = {},
    onOccurrenceSelected = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun RecentOccurrencesSectionForbiddenPreview() = RecentOccurrencesSection(
    occurrences = null,
    error = DataError.Forbidden,
    selectedOccurrenceId = null,
    onRetry = {},
    onOccurrenceSelected = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun RecentOccurrencesSectionEmptyPreview() = RecentOccurrencesSection(
    occurrences = emptyList(),
    error = null,
    selectedOccurrenceId = null,
    onRetry = {},
    onOccurrenceSelected = {},
)
