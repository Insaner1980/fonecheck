package com.insaner.fonecheck.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialDestinationTest {
    @Test
    fun `every launch starts at home`() {
        assertEquals(Home, initialDestination())
    }
}
