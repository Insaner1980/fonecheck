package com.insaner.fonecheck.export

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.AttentionFillLight
import com.insaner.fonecheck.ui.theme.AttentionLight
import com.insaner.fonecheck.ui.theme.FailLight
import com.insaner.fonecheck.ui.theme.InkLight
import com.insaner.fonecheck.ui.theme.PassLight
import kotlin.math.ceil

/**
 * Paper colours. The page is white, not the app's panel, so only the verdict hues are shared with the
 * app: they are its text-safe light roles, which clear 4.5:1 on white as well.
 */
internal object PdfPalette {
    val ink = InkLight.toArgb()
    val meta = Color.rgb(73, 78, 83)
    val rule = Color.rgb(205, 208, 210)

    fun text(mark: PdfMark): Int =
        when (mark) {
            PdfMark.PASS -> PassLight.toArgb()
            PdfMark.FAIL -> FailLight.toArgb()
            PdfMark.WARNING -> AttentionLight.toArgb()
            PdfMark.PARTIAL, PdfMark.NOT_MEASURED, PdfMark.NOT_AVAILABLE -> meta
        }

    fun fill(mark: PdfMark): Int = if (mark == PdfMark.WARNING) AttentionFillLight.toArgb() else text(mark)
}

/** StaticLayout is retained: the exact shaped text that was measured is also drawn. */
internal class AndroidPdfLayout(
    private val context: Context,
    private val labels: PdfReportLabels,
) {
    private val regular = requireNotNull(ResourcesCompat.getFont(context, R.font.dm_sans_regular))
    private val medium = requireNotNull(ResourcesCompat.getFont(context, R.font.dm_sans_medium))
    private val mono = requireNotNull(ResourcesCompat.getFont(context, R.font.jetbrains_mono_regular))
    private val drawableRows = mutableListOf<DrawableRow>()
    internal val observationRows = linkedMapOf<String, List<Int>>()
    private val rulePaint =
        Paint().apply {
            color = PdfPalette.rule
            strokeWidth = 0.5f
        }
    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private data class Slice(
        val layout: StaticLayout,
        val line: Int,
        val x: Float,
        val baselineOffset: Float = 0f,
    )

    /** [centerY] is measured from the top of the row. */
    private data class PlacedMark(
        val mark: PdfMark,
        val x: Float,
        val centerY: Float,
    )

    private data class DrawableRow(
        val slices: List<Slice>,
        val paddingTop: Float,
        val rule: Boolean,
        val marks: List<PlacedMark> = emptyList(),
    )

    fun paginate(blocks: List<PdfTextBlock>): List<List<PdfPositionedRow>> {
        val groups = mutableListOf<PdfMeasuredGroup>()
        var categoryHeading = emptyList<PdfMeasuredRow>()
        var findingHeight = 0f
        var index = 0
        while (index < blocks.size) {
            val block = blocks[index]
            if (block.endsCategories) categoryHeading = emptyList()
            val rows = measure(block)
            if (block.finding) {
                val height = rows.sumOf { it.height.toDouble() }.toFloat()
                if (findingHeight + height > FINDINGS_HEIGHT) {
                    index++
                    continue
                }
                findingHeight += height
            }
            if (block.startsCategory) {
                // A category that runs onto a new page says so, rather than looking like a second category.
                categoryHeading = measure(block.copy(text = "${block.text} (${labels.continued})"))
            } else if (block.category != null && block.group == null) {
                categoryHeading = categoryHeading + rows
            }
            if (block.group != null) {
                val (lastIndex, observation) = measureObservation(blocks, index, rows)
                index = lastIndex
                observationRows[block.group] = observation.map { it.token }
                val continuation =
                    measure(PdfTextBlock("${labels.observation}: ${labels.continued}", PdfTextStyle.META))
                groups += PdfMeasuredGroup(observation, repeatedHeading = categoryHeading, continuation = continuation)
            } else {
                groups += PdfMeasuredGroup(rows, keepWithNext = block.keepWithNext)
            }
            index++
        }
        return PdfLayoutEngine.paginate(groups, CONTENT_HEIGHT)
    }

    private fun measureObservation(
        blocks: List<PdfTextBlock>,
        startIndex: Int,
        firstRows: List<PdfMeasuredRow>,
    ): Pair<Int, List<PdfMeasuredRow>> {
        val group = blocks[startIndex].group
        val rows = firstRows.toMutableList()
        var index = startIndex
        while (index + 1 < blocks.size && blocks[index + 1].group == group) {
            index++
            rows += measure(blocks[index])
        }
        return index to rows
    }

    private fun paint(
        style: PdfTextStyle,
        tint: Int? = null,
    ) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = style.size
        color = tint ?: if (style == PdfTextStyle.META) PdfPalette.meta else PdfPalette.ink
        typeface =
            when (style) {
                PdfTextStyle.TITLE, PdfTextStyle.HEADING, PdfTextStyle.CATEGORY -> medium
                PdfTextStyle.MONO -> mono
                else -> regular
            }
        textLocale = context.resources.configuration.locales[0]
    }

    @SuppressLint("WrongConstant") // Layout's API 23 break-strategy constant is valid for StaticLayout on minSdk 26.
    private fun textLayout(
        text: String,
        style: PdfTextStyle,
        width: Int,
        tint: Int? = null,
    ): StaticLayout {
        val paint = paint(style, tint)
        val displayText = readableGlyphs(text, paint)
        return StaticLayout.Builder
            .obtain(displayText, 0, displayText.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(true)
            .setLineSpacing(1.5f, 1f)
            .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
            .apply { if (Build.VERSION.SDK_INT >= 28) setUseLineSpacingFromFallbacks(true) }
            .build()
    }

    /** Keep supported fallback glyphs; name unsupported code points instead of printing tofu. */
    private fun readableGlyphs(
        text: String,
        paint: TextPaint,
    ): String =
        buildString {
            text.codePoints().forEach { code ->
                val glyph = String(Character.toChars(code))
                val combining =
                    Character.getType(code) in
                        setOf(
                            Character.NON_SPACING_MARK.toInt(),
                            Character.COMBINING_SPACING_MARK.toInt(),
                            Character.ENCLOSING_MARK.toInt(),
                            Character.FORMAT.toInt(),
                        )
                append(
                    if (Character.isWhitespace(code) || combining || paint.hasGlyph(glyph)) {
                        glyph
                    } else {
                        "[U+${code.toString(16).uppercase(java.util.Locale.ROOT).padStart(4, '0')}]"
                    },
                )
            }
        }

    private fun measure(block: PdfTextBlock): List<PdfMeasuredRow> {
        if (block.legend.isNotEmpty()) return measureLegend(block)
        val texts = listOf(block.text) + block.columns
        val table = texts.size == 3
        val inset = if (block.mark != null) MARK_INSET else 0f
        val offsets = if (table) listOf(0f, VALUE_X, STATUS_X + inset) else listOf(inset)
        val widths =
            if (table) {
                listOf(LABEL_WIDTH, VALUE_WIDTH, (CONTENT_WIDTH - STATUS_X - inset).toInt())
            } else {
                listOf((CONTENT_WIDTH - inset).toInt())
            }
        val layouts =
            texts.mapIndexed { index, text ->
                val style = block.columnStyles.getOrNull(index) ?: block.style
                // The status text of a table row takes its verdict's colour; a single line stays in ink.
                val tint = block.mark?.takeIf { table && index == texts.lastIndex }?.let(PdfPalette::text)
                textLayout(text, style, widths[index], tint)
            }
        val lineCount = layouts.maxOf { it.lineCount }
        return (0 until lineCount).map { line ->
            val unaligned =
                layouts.mapIndexedNotNull { index, layout ->
                    if (line < layout.lineCount) Slice(layout, line, offsets[index]) else null
                }
            val baseline = unaligned.maxOf { it.layout.getLineBaseline(line) - it.layout.getLineTop(line) }
            val slices =
                unaligned.map { slice ->
                    slice.copy(
                        baselineOffset =
                            (baseline - slice.layout.getLineBaseline(line) + slice.layout.getLineTop(line)).toFloat(),
                    )
                }
            val top = rowPaddingTop(block, line)
            val bottom = if (line == lineCount - 1) 3f else 0f
            val height =
                slices.maxOf { it.layout.getLineBottom(line) - it.layout.getLineTop(line) + it.baselineOffset } + top +
                    bottom
            // Every text has a first line, so the last slice of line 0 is the text the mark precedes.
            val marked = slices.last()
            val marks =
                if (line == 0 && block.mark != null) {
                    val centerY = top + marked.baselineOffset + markCenter(marked.layout)
                    listOf(PlacedMark(block.mark, marked.x - MARK_INSET, centerY))
                } else {
                    emptyList()
                }
            val token = drawableRows.size
            drawableRows +=
                DrawableRow(
                    slices,
                    top,
                    line == 0 && (block.startsCategory || (block.group != null && block.columns.isNotEmpty())),
                    marks,
                )
            PdfMeasuredRow(token, height)
        }
    }

    /** Legend entries flow along a line and wrap as whole entries. */
    private fun measureLegend(block: PdfTextBlock): List<PdfMeasuredRow> {
        val lines = mutableListOf(mutableListOf<Pair<PdfMark, Slice>>())
        var x = 0f
        block.legend.forEach { (mark, label) ->
            val paint = paint(block.style)
            val width =
                (ceil(paint.measureText(readableGlyphs(label, paint))).toInt() + 1)
                    .coerceAtMost((CONTENT_WIDTH - MARK_INSET).toInt())
            if (x > 0f && x + MARK_INSET + width > CONTENT_WIDTH) {
                lines.add(mutableListOf())
                x = 0f
            }
            lines.last().add(mark to Slice(textLayout(label, block.style, width), 0, x + MARK_INSET))
            x += MARK_INSET + width + LEGEND_GAP
        }
        return lines.map { entries ->
            val height = entries.maxOf { (_, slice) -> slice.layout.getLineBottom(0) - slice.layout.getLineTop(0) }
            val token = drawableRows.size
            drawableRows +=
                DrawableRow(
                    entries.map { it.second },
                    0f,
                    false,
                    entries.map { (mark, slice) -> PlacedMark(mark, slice.x - MARK_INSET, markCenter(slice.layout)) },
                )
            PdfMeasuredRow(token, height + 3f)
        }
    }

    /** Centres a mark on the lower-case height of the first line it sits beside. */
    private fun markCenter(layout: StaticLayout): Float =
        (layout.getLineBaseline(0) - layout.getLineTop(0)) - layout.paint.textSize * X_HEIGHT_CENTER

    private fun rowPaddingTop(
        block: PdfTextBlock,
        line: Int,
    ): Float {
        if (line != 0) return 0f
        return when {
            block.style in setOf(PdfTextStyle.TITLE, PdfTextStyle.HEADING, PdfTextStyle.CATEGORY) -> 9f
            block.columns.isNotEmpty() && block.style == PdfTextStyle.META -> 6f
            block.group != null && block.columns.isNotEmpty() -> 5f
            block.columns.isNotEmpty() -> 2f
            else -> 0f
        }
    }

    fun drawRow(
        canvas: Canvas,
        positioned: PdfPositionedRow,
        left: Float,
        top: Float,
    ) {
        val row = drawableRows[positioned.row.token]
        val y = top + positioned.top
        if (row.rule) canvas.drawLine(left, y, left + CONTENT_WIDTH, y, rulePaint)
        row.slices.forEach { slice ->
            val lineTop = slice.layout.getLineTop(slice.line)
            val lineBottom = slice.layout.getLineBottom(slice.line)
            canvas.withSave {
                canvas.translate(left + slice.x, y + row.paddingTop + slice.baselineOffset)
                canvas.clipRect(0f, 0f, slice.layout.width.toFloat(), (lineBottom - lineTop).toFloat())
                canvas.translate(0f, -lineTop.toFloat())
                slice.layout.draw(canvas)
            }
        }
        row.marks.forEach { drawMark(canvas, it.mark, left + it.x, y + it.centerY) }
    }

    /** Each verdict has its own shape, so a greyscale print still tells them apart. */
    private fun drawMark(
        canvas: Canvas,
        mark: PdfMark,
        x: Float,
        centerY: Float,
    ) {
        val half = MARK_SIZE / 2
        val centerX = x + half
        val bounds = RectF(x, centerY - half, x + MARK_SIZE, centerY + half)
        markPaint.color = PdfPalette.fill(mark)
        markPaint.strokeWidth = MARK_STROKE

        fun solid() = markPaint.apply { style = Paint.Style.FILL }

        fun outline() = markPaint.apply { style = Paint.Style.STROKE }
        when (mark) {
            PdfMark.PASS -> canvas.drawCircle(centerX, centerY, half, solid())
            PdfMark.FAIL -> {
                canvas.drawRect(bounds, solid())
                val inset = MARK_SIZE * 0.28f
                val cross =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = MARK_STROKE
                    }
                canvas.drawLine(x + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset, cross)
                canvas.drawLine(x + inset, bounds.bottom - inset, bounds.right - inset, bounds.top + inset, cross)
            }
            PdfMark.WARNING -> {
                val triangle =
                    Path().apply {
                        moveTo(centerX, bounds.top)
                        lineTo(bounds.right, bounds.bottom)
                        lineTo(x, bounds.bottom)
                        close()
                    }
                canvas.drawPath(triangle, solid())
            }
            PdfMark.PARTIAL -> {
                canvas.drawArc(bounds, 90f, 180f, true, solid())
                val ring = RectF(bounds).apply { inset(MARK_STROKE / 2, MARK_STROKE / 2) }
                canvas.drawOval(ring, outline())
            }
            PdfMark.NOT_MEASURED -> {
                val ring = RectF(bounds).apply { inset(MARK_STROKE / 2, MARK_STROKE / 2) }
                canvas.drawOval(ring, outline())
            }
            PdfMark.NOT_AVAILABLE -> canvas.drawLine(x, centerY, bounds.right, centerY, outline())
        }
    }

    fun drawLogo(canvas: Canvas) {
        val logo = requireNotNull(ResourcesCompat.getDrawable(context.resources, R.drawable.pdf_logo, null))
        // Original 432-unit viewport. Align its visible bounds without distorting the paths.
        canvas.withSave {
            canvas.translate(42f, 32f)
            canvas.scale(1f / 3f, 1f / 3f)
            canvas.translate(-108f, -147.443f)
            logo.setBounds(0, 0, 432, 432)
            logo.draw(canvas)
        }
    }

    fun drawMarginText(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        width: Int,
    ) {
        val layout = textLayout(text, PdfTextStyle.META, width)
        canvas.withSave {
            canvas.translate(left, top)
            layout.draw(canvas)
        }
    }

    companion object {
        const val CONTENT_TOP = 88f
        const val CONTENT_HEIGHT = 692f
        const val CONTENT_WIDTH = 511
        private const val FINDINGS_HEIGHT = 160f
        private const val LABEL_WIDTH = 200
        private const val VALUE_X = 210f
        private const val VALUE_WIDTH = 180
        private const val STATUS_X = 400f
        private const val MARK_SIZE = 7.5f
        private const val MARK_STROKE = 1.1f
        private const val MARK_INSET = 13f
        private const val LEGEND_GAP = 14f
        private const val X_HEIGHT_CENTER = 0.34f
    }
}
