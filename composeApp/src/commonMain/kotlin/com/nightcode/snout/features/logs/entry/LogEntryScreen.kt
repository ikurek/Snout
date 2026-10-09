package com.nightcode.snout.features.logs.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatLogTime
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.CodeBlock
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.features.logs.components.LogSeverityTag
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.TestLogEntries
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.logs_entry_attributes
import snout.composeapp.generated.resources.logs_entry_event
import snout.composeapp.generated.resources.logs_entry_logger
import snout.composeapp.generated.resources.logs_entry_message
import snout.composeapp.generated.resources.logs_entry_resource
import snout.composeapp.generated.resources.logs_entry_span
import snout.composeapp.generated.resources.logs_entry_trace

private const val LoadingRowCount = 4

/** Attribute values such as SQL or user agents run long; four lines keep the screen scannable. */
private const val AttributeValueLines = 4

/** One log line in full: the whole message, its trace and every attribute. Titled with its service. */
@Composable
internal fun LogEntryScreen(
    service: String,
    state: CachedUiState<LogEntry>,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    ListScreenScaffold(
        title = service,
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())
        when (state) {
            CachedUiState.Loading, is CachedUiState.Empty -> SkeletonRows(modifier = modifier, count = LoadingRowCount)
            is CachedUiState.Error -> ErrorPlaceholder(error = state.error, onRetry = onRetry, modifier = modifier)
            is CachedUiState.Content -> LogEntryContent(state.data, modifier, innerPadding.bottomOnly())
        }
    }
}

@Composable
private fun LogEntryContent(entry: LogEntry, modifier: Modifier, contentPadding: PaddingValues) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(contentPadding).padding(bottom = 24.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            LogSeverityTag(entry.severity)
            MetaText(entry.timestamp.formatLogTime())
        }
        SectionLabel(stringResource(Res.string.logs_entry_message))
        CodeBlock(text = entry.body, wrap = true, modifier = Modifier.padding(horizontal = 16.dp))
        SourceRows(entry, modifier = Modifier.padding(top = 12.dp))
        AttributeSection(stringResource(Res.string.logs_entry_attributes), entry.attributes)
        AttributeSection(stringResource(Res.string.logs_entry_resource), entry.resourceAttributes)
    }
}

/** Where the line came from: logger, event, trace and span, whichever the SDK set. */
@Composable
private fun SourceRows(entry: LogEntry, modifier: Modifier = Modifier) {
    val rows = listOfNotNull(
        entry.logger?.let { stringResource(Res.string.logs_entry_logger) to it },
        entry.eventName?.let { stringResource(Res.string.logs_entry_event) to it },
        entry.traceId?.let { stringResource(Res.string.logs_entry_trace) to it },
        entry.spanId?.let { stringResource(Res.string.logs_entry_span) to it },
    )
    if (rows.isEmpty()) return
    Column(modifier = modifier) {
        rows.forEach { (label, value) -> KeyValueRow(label = label, value = value, valueIsCode = true) }
    }
}

@Composable
private fun AttributeSection(title: String, attributes: Map<String, String>) {
    if (attributes.isEmpty()) return
    SectionLabel(title)
    attributes.entries.sortedBy { it.key }.forEach { (key, value) ->
        KeyValueRow(label = key, value = value, valueIsCode = true, valueMaxLines = AttributeValueLines)
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogEntryScreenPreview() = LogEntryScreen(
    service = "checkout-api",
    state = CachedUiState.Content(TestLogEntries.first(), isRefreshing = false),
    onNavigateBack = {},
    onRetry = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogEntryScreenNotFoundPreview() = LogEntryScreen(
    service = "checkout-api",
    state = CachedUiState.Error(DataError.NotFound),
    onNavigateBack = {},
    onRetry = {},
    onOpenInPostHog = {},
)
