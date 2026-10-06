package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private val CellWidth = 132.dp
private const val MaxRows = 12

/** Horizontally scrollable table for SQL style results. */
@Composable
internal fun DataTable(columns: List<String>, rows: List<List<String>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row {
            columns.forEach { column ->
                Text(
                    text = column,
                    style = SnoutTheme.typography.labelMedium,
                    color = SnoutTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(CellWidth).padding(vertical = 6.dp, horizontal = 4.dp),
                )
            }
        }
        HorizontalDivider(color = SnoutTheme.colors.outlineVariant)
        rows.take(MaxRows).forEach { row ->
            Row {
                columns.indices.forEach { index ->
                    Text(
                        text = row.getOrNull(index).orEmpty(),
                        style = SnoutTheme.code.small,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(CellWidth).padding(vertical = 6.dp, horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DataTablePreview() = DataTable(
    columns = listOf("event", "count", "users"),
    rows = listOf(listOf("\$pageview", "4200", "900"), listOf("sign up", "120", "118")),
)
