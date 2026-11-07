package com.nightcode.snout.appwidget

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min

/** Inside the highlights widget's frame; its tiles carry their own padding, so the frame needs less. */
internal val HighlightsPadding = 12.dp

/** Between tiles, both ways, and between the header and the first row. */
internal val TileGap = 6.dp

/** A tile's inset: its text from its edges. */
internal val TileHorizontalPadding = 8.dp
internal val TileVerticalPadding = 6.dp

/** No narrower, so a value of five or six characters still reads at the smallest size. */
private val MinTileWidth = 64.dp

/** No taller: past it the tiles would only grow empty, so the room left goes to the footer. */
private val MaxTileHeight = 96.dp

/** The width over the height a tile looks best at: a card, wider than tall. */
private const val TileShape = 1.8f

/** At most this many tiles side by side, on the widest widgets. */
private const val MaxColumns = 4

/**
 * A line of text's height per sp of its size, with the font padding a RemoteViews' TextView keeps: a little more
 * than Roboto's own, so a tile measured with it never cuts its last line off.
 */
private const val LineHeightPerSp = 1.36f

/**
 * [text]'s width in ems, bold: each character a little wider than Roboto's, so a value measured with it never runs out
 * of room. Digits are all as wide; punctuation and spaces narrower, `m`, `%` and the like wider.
 */
internal fun emWidth(text: String): Float = text.sumOf { char ->
    when {
        char.isDigit() -> DigitEm
        char in ".,: '" -> NarrowEm
        char in "mwMW%@" -> WideEm
        char.isUpperCase() -> CapitalEm
        else -> LetterEm
    }
}.toFloat()

private const val DigitEm = 0.58
private const val NarrowEm = 0.3
private const val WideEm = 0.92
private const val CapitalEm = 0.7
private const val LetterEm = 0.6

/** The sizes a value takes, largest first: the largest the tile has room for, for every tile alike. */
private val ValueSizes = listOf(32, 26, 22, 20, 18, 16, 14)

/** The smallest value a change goes under: smaller, and the tile shows an arrow by its name instead. */
private const val MinValueSpWithChange = 20

/** The size of the note a tile without a value shows in its place. */
internal const val TileNoteSp = 12

/** The size of a tile's name, and of its change under the value. */
internal const val TileCaptionSp = 11

/** What a highlights widget draws, worked out from its size: [columns] by [rows] tiles, each [tileHeight] tall. */
internal data class HighlightsLayout(
    val columns: Int,
    val rows: Int,
    val tileHeight: Dp,
    /** The value's size in sp, the same on every tile, so they line up. */
    val valueSp: Int,
    /** Whether a tile has room for its change under its value; without, an arrow by its name says which way. */
    val showsChange: Boolean,
    val showsFooter: Boolean,
    /** A tile's width inside its padding, which a note's words have to fit. */
    val textWidth: Dp,
) {
    val shown: Int get() = columns * rows
}

private fun line(sp: Int, fontScale: Float): Dp = (sp * LineHeightPerSp * fontScale).dp

/** The least a tile takes: its name over a value at the smallest size, inside [tilePadding] top and bottom. */
internal fun minTileHeight(fontScale: Float, tilePadding: Dp): Dp =
    tilePadding * 2 + line(TileCaptionSp, fontScale) + line(ValueSizes.last(), fontScale)

/**
 * Lays out [count] tiles with values as long as [values] in a grid [width] by [height] (what is left of the widget
 * once its header is drawn), text scaled by [fontScale] and each tile inset by [tilePadding] top and bottom: as many
 * tiles as fit, in the fewest columns that show them, so they are as wide as they can be.
 */
internal fun highlightsLayout(
    width: Dp,
    height: Dp,
    count: Int,
    values: List<String>,
    fontScale: Float,
    footerHeight: Dp,
    tilePadding: Dp,
): HighlightsLayout {
    fun line(sp: Int): Dp = line(sp, fontScale)
    val smallest = ValueSizes.last()
    val minTileHeight = minTileHeight(fontScale, tilePadding)
    val rowsThatFit = ((height + TileGap) / (minTileHeight + TileGap)).toInt().coerceAtLeast(1)
    val columnsThatFit = ((width + TileGap) / (MinTileWidth + TileGap)).toInt().coerceIn(1, MaxColumns)
    fun rows(columns: Int): Int = min(rowsThatFit, (count + columns - 1) / columns).coerceAtLeast(1)
    fun tileHeight(rows: Int): Dp = min(((height - TileGap * (rows - 1)) / rows).value, MaxTileHeight.value).dp
    fun tileWidth(columns: Int): Dp = (width - TileGap * (columns - 1)) / columns
    // Of the columns that show the most tiles, those whose tiles are shaped most like a card: three by three before
    // two by five, two by two before four in a row.
    val most = (1..columnsThatFit).maxOf { min(count, it * rowsThatFit) }
    val columns = (1..columnsThatFit).filter { min(count, it * rowsThatFit) == most }.minBy { columns ->
        val shape = tileWidth(columns) / tileHeight(rows(columns))
        abs(ln(shape / TileShape))
    }
    val rows = rows(columns)
    val tileHeight = tileHeight(rows)
    val tileWidth = tileWidth(columns)
    val textWidth = tileWidth - TileHorizontalPadding * 2
    val widest = values.take(columns * rows).maxOfOrNull(::emWidth) ?: 1f
    val textHeight = tileHeight - tilePadding * 2 - line(TileCaptionSp)
    val showsChange = textHeight >= line(TileCaptionSp) + line(MinValueSpWithChange)
    val valueSp = ValueSizes.firstOrNull { sp ->
        val valueHeight = if (showsChange) textHeight - line(TileCaptionSp) else textHeight
        line(sp) <= valueHeight && (widest * sp * fontScale).dp <= textWidth
    } ?: smallest
    val used = tileHeight * rows + TileGap * (rows - 1)
    return HighlightsLayout(
        columns = columns,
        rows = rows,
        tileHeight = tileHeight,
        valueSp = valueSp,
        showsChange = showsChange,
        showsFooter = height - used >= footerHeight,
        textWidth = textWidth,
    )
}
