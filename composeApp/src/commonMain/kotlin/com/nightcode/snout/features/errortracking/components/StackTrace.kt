package com.nightcode.snout.features.errortracking.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionFrame
import com.nightcode.snout.shared.domain.model.errortracking.OccurrenceException
import com.nightcode.snout.shared.domain.model.errortracking.SourceContext
import com.nightcode.snout.shared.domain.model.errortracking.TestOccurrenceExceptions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_exception_handled
import snout.composeapp.generated.resources.error_tracking_exception_no_frames
import snout.composeapp.generated.resources.error_tracking_exception_unhandled
import snout.composeapp.generated.resources.error_tracking_frame_hide_source
import snout.composeapp.generated.resources.error_tracking_frame_show_source
import snout.composeapp.generated.resources.error_tracking_frame_unknown_function
import snout.composeapp.generated.resources.error_tracking_frames_hide
import snout.composeapp.generated.resources.error_tracking_frames_show
import snout.composeapp.generated.resources.error_tracking_library_frames
import snout.composeapp.generated.resources.ic_chevron_right_24dp

private const val ChevronCollapsedRotation = 90f
private const val ChevronExpandedRotation = 270f

private sealed interface FrameGroup {
    /** [index] is the frame's position in the stack, so its expansion survives other groups opening. */
    data class App(val frame: ExceptionFrame, val index: Int) : FrameGroup

    data class Library(val frames: List<ExceptionFrame>, val index: Int) : FrameGroup
}

/**
 * A stack with no app frame at all (an SDK that marks none, or a crash inside a library) is shown frame by frame
 * rather than folded into a single row, so there is still something to read.
 */
private fun List<ExceptionFrame>.grouped(): List<FrameGroup> {
    if (none { it.inApp }) return mapIndexed { index, frame -> FrameGroup.App(frame, index) }
    val groups = mutableListOf<FrameGroup>()
    forEachIndexed { index, frame ->
        val last = groups.lastOrNull()
        when {
            frame.inApp -> groups += FrameGroup.App(frame, index)
            last is FrameGroup.Library -> groups[groups.lastIndex] = last.copy(frames = last.frames + frame)
            else -> groups += FrameGroup.Library(listOf(frame), index)
        }
    }
    return groups
}

/** The first app frame with source context starts open. [expandLibraryFrames] is for previews. */
@Composable
internal fun ExceptionStackTrace(
    exception: OccurrenceException,
    modifier: Modifier = Modifier,
    expandLibraryFrames: Boolean = false,
) {
    val groups = remember(exception) { exception.frames.grouped() }
    val firstWithSource = remember(exception) {
        groups.filterIsInstance<FrameGroup.App>().firstOrNull { it.frame.context != null }?.index
    }
    val expanded = remember(exception) {
        mutableStateMapOf<Int, Boolean>().apply {
            firstWithSource?.let { put(it, true) }
            if (expandLibraryFrames) groups.filterIsInstance<FrameGroup.Library>().forEach { put(it.index, true) }
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        ExceptionHeader(exception)
        if (groups.isEmpty()) {
            Text(
                text = stringResource(Res.string.error_tracking_exception_no_frames),
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xSmall),
        ) {
            groups.forEach { group ->
                val isExpanded = expanded[group.index()] == true
                val onToggle = { expanded[group.index()] = !isExpanded }
                when (group) {
                    is FrameGroup.App -> AppFrame(group.frame, isExpanded, onToggle)
                    is FrameGroup.Library -> LibraryFrames(group.frames, isExpanded, onToggle)
                }
            }
        }
    }
}

private fun FrameGroup.index() = when (this) {
    is FrameGroup.App -> index
    is FrameGroup.Library -> index
}

@Composable
private fun ExceptionHeader(exception: OccurrenceException) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
        modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Text(
                text = exception.type,
                style = SnoutTheme.code.large,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            exception.handled?.let { handled ->
                if (handled) {
                    Tag(text = stringResource(Res.string.error_tracking_exception_handled))
                } else {
                    Tag(
                        text = stringResource(Res.string.error_tracking_exception_unhandled),
                        tone = StatusTone.Attention,
                    )
                }
            }
        }
        exception.message?.let {
            SelectionContainer {
                Text(text = it, style = SnoutTheme.typography.bodyMedium, color = SnoutTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppFrame(frame: ExceptionFrame, isExpanded: Boolean, onToggle: () -> Unit) {
    val context = frame.context
    val toggleLabel = stringResource(
        if (isExpanded) Res.string.error_tracking_frame_hide_source else Res.string.error_tracking_frame_show_source,
    )
    Surface(
        shape = SnoutTheme.shapes.small,
        color = if (frame.inApp) SnoutTheme.colors.surfaceContainer else SnoutTheme.colors.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (context != null) {
                            Modifier.clickable(onClickLabel = toggleLabel, role = Role.Button, onClick = onToggle)
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = Spacing.small, vertical = Spacing.xSmall),
            ) {
                FrameText(frame, emphasized = frame.inApp, modifier = Modifier.weight(1f))
                if (context != null) Chevron(isExpanded)
            }
            if (context != null) {
                AnimatedVisibility(visible = isExpanded) {
                    SourceContextBlock(
                        context = context,
                        modifier = Modifier.padding(start = Spacing.small, end = Spacing.small, bottom = Spacing.small),
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryFrames(frames: List<ExceptionFrame>, isExpanded: Boolean, onToggle: () -> Unit) {
    val toggleLabel = stringResource(
        if (isExpanded) Res.string.error_tracking_frames_hide else Res.string.error_tracking_frames_show,
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = toggleLabel, role = Role.Button, onClick = onToggle)
                .padding(horizontal = Spacing.small, vertical = Spacing.xSmall),
        ) {
            Text(
                text = pluralStringResource(Res.plurals.error_tracking_library_frames, frames.size, frames.size),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Chevron(isExpanded)
        }
        AnimatedVisibility(visible = isExpanded) {
            Column {
                frames.forEach { frame ->
                    FrameText(
                        frame = frame,
                        emphasized = false,
                        modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xxSmall),
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameText(frame: ExceptionFrame, emphasized: Boolean, modifier: Modifier = Modifier) {
    val location = (frame.source ?: frame.module)?.let { listOfNotNull(it, frame.line, frame.column).joinToString(":") }
    Column(modifier = modifier) {
        Text(
            text = frame.function ?: stringResource(Res.string.error_tracking_frame_unknown_function),
            style = SnoutTheme.code.medium,
            color = if (emphasized) SnoutTheme.colors.onSurface else SnoutTheme.colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        location?.let {
            Text(
                text = it,
                style = SnoutTheme.code.small,
                color = SnoutTheme.colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Chevron(isExpanded: Boolean) {
    val rotation by animateFloatAsState(if (isExpanded) ChevronExpandedRotation else ChevronCollapsedRotation)
    Icon(
        painter = painterResource(Res.drawable.ic_chevron_right_24dp),
        contentDescription = null,
        tint = SnoutTheme.colors.onSurfaceVariant,
        modifier = Modifier.size(20.dp).rotate(rotation),
    )
}

@Composable
private fun SourceContextBlock(context: SourceContext, modifier: Modifier = Modifier) {
    // Monospace, so padding the numbers to the widest aligns the code.
    val numberWidth = context.lines.maxOf { it.number }.toString().length
    Surface(shape = SnoutTheme.shapes.small, color = SnoutTheme.colors.surfaceContainerHigh, modifier = modifier) {
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = Spacing.xSmall),
            ) {
                context.lines.forEach { line ->
                    val isErrorLine = line.number == context.errorLine
                    val color = if (isErrorLine) SnoutTheme.colors.onErrorContainer else SnoutTheme.colors.onSurface
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        modifier = Modifier
                            .then(if (isErrorLine) Modifier.background(SnoutTheme.colors.errorContainer) else Modifier)
                            .padding(horizontal = Spacing.small),
                    ) {
                        Text(
                            text = line.number.toString().padStart(numberWidth),
                            style = SnoutTheme.code.small,
                            color = if (isErrorLine) color else SnoutTheme.colors.onSurfaceVariant,
                        )
                        Text(text = line.code, style = SnoutTheme.code.small, color = color, softWrap = false)
                    }
                }
            }
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExceptionStackTracePreview() = ExceptionStackTrace(exception = TestOccurrenceExceptions.first())

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExceptionStackTraceLibraryFramesPreview() = ExceptionStackTrace(
    exception = TestOccurrenceExceptions.first().copy(handled = true),
    expandLibraryFrames = true,
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExceptionStackTraceNoFramesPreview() = ExceptionStackTrace(
    exception = TestOccurrenceExceptions.first().copy(message = null, handled = null, frames = emptyList()),
)
