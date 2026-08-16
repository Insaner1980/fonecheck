package com.insaner.fonecheck.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class LongValueRowTest {
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
