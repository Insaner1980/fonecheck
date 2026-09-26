package com.insaner.fonecheck.export

/** A row is already measured with the production text layout. Tokens refer to drawable rows. */
data class PdfMeasuredRow(
    val token: Int,
    val height: Float,
)

data class PdfMeasuredGroup(
    val rows: List<PdfMeasuredRow>,
    val keepWithNext: Boolean = false,
    val repeatedHeading: List<PdfMeasuredRow> = emptyList(),
    val continuation: List<PdfMeasuredRow> = emptyList(),
)

data class PdfPositionedRow(
    val row: PdfMeasuredRow,
    val top: Float,
)

object PdfLayoutEngine {
    fun paginate(
        groups: List<PdfMeasuredGroup>,
        contentHeight: Float,
    ): List<List<PdfPositionedRow>> {
        require(contentHeight > 0)
        val pages = mutableListOf<List<PdfPositionedRow>>()
        var page = mutableListOf<PdfPositionedRow>()
        var used = 0f

        fun append(rows: List<PdfMeasuredRow>) {
            rows.forEach { row ->
                require(row.height > 0 && row.height <= contentHeight)
                page += PdfPositionedRow(row, used)
                used += row.height
            }
        }

        fun nextPage(heading: List<PdfMeasuredRow>) {
            if (page.isNotEmpty()) pages += page
            page = mutableListOf()
            used = 0f
            append(heading)
        }
        groups.forEachIndexed { index, group ->
            if (group.rows.isEmpty()) return@forEachIndexed
            val headingHeight = group.repeatedHeading.sumOf { it.height.toDouble() }.toFloat()
            val needed = keptGroupHeight(groups, index, contentHeight)
            if (page.isNotEmpty() && used + needed > contentHeight && needed <= contentHeight - headingHeight) {
                nextPage(group.repeatedHeading)
            }
            // Oversized observations split only between measured text rows. Every split progresses.
            group.rows.forEachIndexed { rowIndex, row ->
                if (used + row.height > contentHeight) {
                    val heading = pageBreakHeading(group, rowIndex)
                    require(heading.sumOf { it.height.toDouble() } + row.height <= contentHeight) {
                        "Page cannot hold a continuation heading and a text row."
                    }
                    nextPage(heading)
                }
                append(listOf(row))
            }
        }
        if (page.isNotEmpty()) pages += page
        return pages
    }

    private fun pageBreakHeading(
        group: PdfMeasuredGroup,
        rowIndex: Int,
    ): List<PdfMeasuredRow> = group.repeatedHeading + if (rowIndex > 0) group.continuation else emptyList()

    private fun keptGroupHeight(
        groups: List<PdfMeasuredGroup>,
        index: Int,
        contentHeight: Float,
    ): Float {
        // Keep a chain of headings with the next complete observation when it fits.
        var needed = groups[index].rows.sumOf { it.height.toDouble() }.toFloat()
        var next = index
        while (groups[next].keepWithNext && next + 1 < groups.size) {
            next++
            val following = groups[next].rows
            val full = following.sumOf { it.height.toDouble() }.toFloat()
            needed += if (needed + full <= contentHeight) full else following.first().height
        }
        return needed
    }
}
