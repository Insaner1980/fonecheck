package com.insaner.fonecheck.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class LongValueRowTest {
    @Test
    fun `a value that fits keeps the one-line layout`() {
        assertEquals(
            false,
            shouldUseLongValueLayout(
                rowWidth = 400,
                labelWidth = 100,
                labelMaxWidth = 160,
                valueWidth = 284,
                rowGap = 16,
            ),
        )
    }

    @Test
    fun `a value that does not fit uses the two-line layout`() {
        assertEquals(
            true,
            shouldUseLongValueLayout(
                rowWidth = 400,
                labelWidth = 100,
                labelMaxWidth = 160,
                valueWidth = 285,
                rowGap = 16,
            ),
        )
    }

    @Test
    fun `a placeholder always keeps the one-line layout`() {
        assertEquals(
            false,
            shouldUseLongValueLayout(
                rowWidth = 400,
                labelWidth = 100,
                labelMaxWidth = 160,
                valueWidth = null,
                rowGap = 16,
            ),
        )
    }

    @Test
    fun `a break opportunity follows every hyphen dot and comma`() {
        assertEquals(
            "a-${ZWSP}b.${ZWSP}c,${ZWSP}d",
            withTokenBreakOpportunities("a-b.c,d"),
        )
    }

    @Test
    fun `a token without separators keeps no break opportunities`() {
        assertEquals("BP41250822011", withTokenBreakOpportunities("BP41250822011"))
    }

    @Test
    fun `a trailing separator gains no break opportunity`() {
        assertEquals("release-${ZWSP}keys.", withTokenBreakOpportunities("release-keys."))
    }

    @Test
    fun `an empty value stays empty`() {
        assertEquals("", withTokenBreakOpportunities(""))
    }

    @Test
    fun `the visible value is unchanged once the break hints are removed`() {
        val fingerprint = "google/panther/panther:16/BP41.250822.011/13729421:user/release-keys"
        assertEquals(
            fingerprint,
            withTokenBreakOpportunities(fingerprint).replace(ZWSP.toString(), ""),
        )
    }

    private companion object {
        const val ZWSP = '\u200B'
    }
}
