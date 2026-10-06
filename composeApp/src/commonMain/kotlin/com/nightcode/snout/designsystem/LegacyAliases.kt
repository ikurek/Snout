package com.nightcode.snout.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.SegmentedControl
import com.nightcode.snout.designsystem.molecules.StatCard

// Temporary bridges from the previous component names; removed once every screen uses the new ones.

internal enum class LabelChipTone { Neutral, Success, Warning, Error, Info }

@Composable
internal fun LabelChip(text: String, modifier: Modifier = Modifier, tone: LabelChipTone = LabelChipTone.Neutral) =
    Tag(
        text = text,
        modifier = modifier,
        tone = when (tone) {
            LabelChipTone.Neutral -> StatusTone.Neutral
            LabelChipTone.Success -> StatusTone.Active
            LabelChipTone.Warning, LabelChipTone.Error -> StatusTone.Attention
            LabelChipTone.Info -> StatusTone.Info
        },
    )

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) = SectionLabel(text = text, modifier = modifier)

@Composable
internal fun InfoNote(text: String, modifier: Modifier = Modifier) = InfoBanner(text = text, modifier = modifier)

@Composable
internal fun InlineError(text: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) =
    InlineErrorBanner(text = text, modifier = modifier, onAction = onRetry)

@Composable
internal fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    changePercent: Double? = null,
    isIncreaseBad: Boolean = false,
    secondary: String? = null,
) = StatCard(
    label = label,
    value = value,
    modifier = modifier,
    changePercent = changePercent,
    lowerIsBetter = isIncreaseBad,
    comparison = secondary,
)

@Composable
internal fun <T> SegmentedChoice(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) = SegmentedControl(options = options, selected = selected, onSelect = onSelect, label = label, modifier = modifier)

@Composable
internal fun LoadingIndicator(modifier: Modifier = Modifier) = SkeletonRows(modifier = modifier)
