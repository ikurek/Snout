package com.nightcode.snout.shared.data.errortracking.mapper

import com.nightcode.snout.shared.api.errortracking.model.ExceptionDto
import com.nightcode.snout.shared.api.errortracking.model.ExceptionFrameDto
import com.nightcode.snout.shared.api.errortracking.model.FrameContextDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLResultsDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionFrame
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionOccurrence
import com.nightcode.snout.shared.domain.model.errortracking.OccurrenceException
import com.nightcode.snout.shared.domain.model.errortracking.SourceContext
import com.nightcode.snout.shared.domain.model.errortracking.SourceLine
import kotlin.time.Instant

private const val UnknownException = "Error"

/** The occurrences query's columns, in SELECT order. */
private enum class OccurrenceColumn {
    Id,
    Timestamp,
    Os,
    OsVersion,
    AppVersion,
    Browser,
    BrowserVersion,
    DeviceModel,
    Url,
    Screen,
    Library,
    DistinctId,
    PersonEmail,
    PersonName,
}

/** Rows of the occurrences query; rows without an id or a timestamp are skipped. */
internal fun HogQLResultsDto.toOccurrences(): List<ExceptionOccurrence> = results.mapNotNull { row ->
    fun column(column: OccurrenceColumn): String? = row.getOrNull(column.ordinal).string().nullIfBlank()
    val id = column(OccurrenceColumn.Id) ?: return@mapNotNull null
    val timestamp = column(OccurrenceColumn.Timestamp)?.toInstantOrNull() ?: return@mapNotNull null
    ExceptionOccurrence(
        id = id,
        timestamp = timestamp,
        os = column(OccurrenceColumn.Os),
        osVersion = column(OccurrenceColumn.OsVersion),
        appVersion = column(OccurrenceColumn.AppVersion),
        browser = column(OccurrenceColumn.Browser),
        browserVersion = column(OccurrenceColumn.BrowserVersion),
        deviceModel = column(OccurrenceColumn.DeviceModel),
        url = column(OccurrenceColumn.Url),
        screen = column(OccurrenceColumn.Screen),
        library = column(OccurrenceColumn.Library),
        person = column(OccurrenceColumn.PersonName)
            ?: column(OccurrenceColumn.PersonEmail)
            ?: column(OccurrenceColumn.DistinctId),
    )
}

internal fun ExceptionDto.toOccurrenceException() = OccurrenceException(
    type = type.nullIfBlank() ?: UnknownException,
    message = value.nullIfBlank(),
    handled = mechanism?.handled,
    // PostHog keeps the outermost call first; the frame that threw reads first on screen.
    frames = stacktrace?.frames.orEmpty().asReversed().map { it.toFrame() },
)

private fun ExceptionFrameDto.toFrame(): ExceptionFrame {
    val line = (line ?: lineNumber)?.takeIf { it > 0 }
    return ExceptionFrame(
        function = (resolvedName ?: function ?: mangledName).nullIfBlank(),
        module = module.nullIfBlank(),
        source = (source ?: filename).nullIfBlank(),
        line = line,
        column = (column ?: columnNumber)?.takeIf { it > 0 },
        inApp = inApp,
        context = context?.toSourceContext(line),
    )
}

/** Null when PostHog has no source for the frame, which it sends as an empty context. */
private fun FrameContextDto.toSourceContext(frameLine: Int?): SourceContext? {
    val errorLine = line?.number ?: frameLine ?: return null
    val lines = (before + listOfNotNull(line) + after).map { SourceLine(it.number, it.line) }
    return lines.takeIf { it.isNotEmpty() }?.let { SourceContext(it, errorLine) }
}

private fun String.toInstantOrNull(): Instant? = runCatching { Instant.parse(this) }.getOrNull()
