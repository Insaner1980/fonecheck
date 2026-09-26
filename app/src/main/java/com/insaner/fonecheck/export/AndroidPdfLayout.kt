package com.insaner.fonecheck.export

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import com.insaner.fonecheck.R

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
            color = Color.rgb(205, 208, 210)
            strokeWidth = 0.5f
        }

    private data class Slice(
        val layout: StaticLayout,
        val line: Int,
        val x: Float,
        val baselineOffset: Float = 0f,
    )

    private data class DrawableRow(
        val slices: List<Slice>,
        val paddingTop: Float,
        val rule: Boolean,
    )

    fun paginate(blocks: List<PdfTextBlock>): List<List<PdfPositionedRow>> {
        val groups = mutableListOf<PdfMeasuredGroup>()
        var categoryHeading = emptyList<PdfMeasuredRow>()
        var findingHeight = 0f
        var index = 0
        while (index < blocks.size) {
            val block = blocks[index]
            if (block.endsCategories || block.startsCategory) categoryHeading = emptyList()
            val rows = measure(block)
            if (block.finding) {
                val height = rows.sumOf { it.height.toDouble() }.toFloat()
                if (findingHeight + height > FINDINGS_HEIGHT) {
                    index++
                    continue
                }
                findingHeight += height
            }
            if (block.category != null && block.group == null) categoryHeading = categoryHeading + rows
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

    private fun paint(style: PdfTextStyle) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = style.size
            color = if (style == PdfTextStyle.META) Color.rgb(73, 78, 83) else Color.rgb(23, 25, 28)
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
    ): StaticLayout {
        val paint = paint(style)
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
        val texts = listOf(block.text) + block.columns
        val widths = if (texts.size == 3) listOf(220, 163, 108) else listOf(CONTENT_WIDTH)
        val offsets = if (texts.size == 3) listOf(0f, 230f, 403f) else listOf(0f)
        val layouts =
            texts.mapIndexed { index, text ->
                val style = if (block.group != null && index == 1) PdfTextStyle.MONO else block.style
                textLayout(text, style, widths[index])
            }
        return (0 until layouts.maxOf { it.lineCount }).map { line ->
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
            val last = line == layouts.maxOf { it.lineCount } - 1
            val bottom = if (last) 3f else 0f
            val height =
                slices.maxOf { it.layout.getLineBottom(line) - it.layout.getLineTop(line) + it.baselineOffset } + top +
                    bottom
            val token = drawableRows.size
            drawableRows +=
                DrawableRow(
                    slices,
                    top,
                    line == 0 && (block.startsCategory || (block.group != null && block.columns.isNotEmpty())),
                )
            PdfMeasuredRow(token, height)
        }
    }

    private fun rowPaddingTop(
        block: PdfTextBlock,
        line: Int,
    ): Float {
        if (line != 0) return 0f
        return when {
            block.style in setOf(PdfTextStyle.TITLE, PdfTextStyle.HEADING, PdfTextStyle.CATEGORY) -> 9f
            block.columns.isNotEmpty() -> 5f
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
        private const val FINDINGS_HEIGHT = 132f
    }
}
