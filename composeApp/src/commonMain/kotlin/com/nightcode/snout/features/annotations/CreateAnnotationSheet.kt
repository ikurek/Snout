package com.nightcode.snout.features.annotations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextField
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.organisms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.annotation_create_content_label
import snout.composeapp.generated.resources.annotation_create_content_placeholder
import snout.composeapp.generated.resources.annotation_create_date_label
import snout.composeapp.generated.resources.annotation_create_error_forbidden
import snout.composeapp.generated.resources.annotation_create_error_generic
import snout.composeapp.generated.resources.annotation_create_save
import snout.composeapp.generated.resources.annotation_create_title
import snout.composeapp.generated.resources.dialog_cancel
import snout.composeapp.generated.resources.dialog_ok
import snout.composeapp.generated.resources.ic_schedule_24dp
import kotlin.time.Clock
import kotlin.time.Instant

private const val MillisPerDay = 86_400_000L
private val MarkerTime = LocalTime(12, 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateAnnotationSheet(
    isVisible: Boolean,
    status: CreateStatus,
    onDismiss: () -> Unit,
    onCreate: (content: String, dateMarker: Instant) -> Unit,
) {
    val contentState = rememberTextFieldState()
    val timeZone = TimeZone.currentSystemDefault()
    var selectedDate by rememberSaveable {
        mutableStateOf(Clock.System.now().toLocalDateTime(timeZone).date.toString())
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateMarker = LocalDate.parse(selectedDate).atTime(MarkerTime).toInstant(timeZone)

    AnimatedBottomSheet(isVisible = isVisible, onDismissRequest = onDismiss) {
        CreateAnnotationContent(
            contentState = contentState,
            dateMarker = dateMarker,
            status = status,
            onPickDate = { showDatePicker = true },
            onSave = { onCreate(contentState.text.toString(), dateMarker) },
        )
    }

    if (showDatePicker) {
        AnnotationDatePickerDialog(
            initialDate = LocalDate.parse(selectedDate),
            onConfirm = { date ->
                selectedDate = date.toString()
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun CreateAnnotationContent(
    contentState: TextFieldState,
    dateMarker: Instant,
    status: CreateStatus,
    onPickDate: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Spacing.large).imePadding()) {
        Text(text = stringResource(Res.string.annotation_create_title), style = SnoutTheme.typography.titleLarge)
        VerticalSpacer(Spacing.large)
        PrimaryTextField(
            state = contentState,
            label = stringResource(Res.string.annotation_create_content_label),
            placeholder = stringResource(Res.string.annotation_create_content_placeholder),
            enabled = status !is CreateStatus.Saving,
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 6),
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(Spacing.small)
        KeyValueRow(
            label = stringResource(Res.string.annotation_create_date_label),
            value = dateMarker.formatShortDate(),
            leadingIcon = painterResource(Res.drawable.ic_schedule_24dp),
            onClick = onPickDate,
            showChevron = false,
            contentPadding = PaddingValues(vertical = Spacing.small),
        )
        if (status is CreateStatus.Failed) {
            InlineErrorBanner(text = stringResource(status.error.createFailedMessage()))
            VerticalSpacer(Spacing.small)
        }
        VerticalSpacer(Spacing.large)
        PrimaryButton(
            text = stringResource(Res.string.annotation_create_save),
            onClick = onSave,
            enabled = contentState.text.isNotBlank(),
            loading = status is CreateStatus.Saving,
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(Spacing.xxLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnotationDatePickerDialog(initialDate: LocalDate, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.toEpochDays() * MillisPerDay)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis == null) {
                        onDismiss()
                    } else {
                        onConfirm(LocalDate.fromEpochDays((millis / MillisPerDay).toInt()))
                    }
                },
            ) {
                Text(stringResource(Res.string.dialog_ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.dialog_cancel)) } },
    ) {
        DatePicker(state = pickerState)
    }
}

private fun DataError.createFailedMessage() = actionFailedMessage(
    generic = Res.string.annotation_create_error_generic,
    forbidden = Res.string.annotation_create_error_forbidden,
)

/** The sheet's content on its `surfaceContainer` ground; the modal itself does not render in a static preview. */
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun CreateAnnotationSheetPreview() = CreateAnnotationContent(
    contentState = rememberTextFieldState(initialText = "Released 2.4 with the new checkout"),
    dateMarker = Instant.parse("2026-10-06T12:00:00Z"),
    status = CreateStatus.Idle,
    onPickDate = {},
    onSave = {},
    modifier = Modifier.background(SnoutTheme.colors.surfaceContainer),
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun CreateAnnotationSheetErrorPreview() = CreateAnnotationContent(
    contentState = rememberTextFieldState(initialText = "Released 2.4 with the new checkout"),
    dateMarker = Instant.parse("2026-10-06T12:00:00Z"),
    status = CreateStatus.Failed(DataError.Forbidden),
    onPickDate = {},
    onSave = {},
    modifier = Modifier.background(SnoutTheme.colors.surfaceContainer),
)
