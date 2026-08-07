package com.insaner.fonecheck.navigation

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticDestinationTest {
    @Test
    fun implementedDestinationsFollowCanonicalOrderAndIncludeThermal() {
        val expected = DiagnosticCatalog.categories.filterNot { it == DiagnosticCategoryId.STORAGE }

        assertEquals(expected, diagnosticDestinations.map { it.category })
        assertEquals(ThermalTest, diagnosticDestinations.single { it.category == DiagnosticCategoryId.THERMAL }.route)
    }
}
