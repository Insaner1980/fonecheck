package com.insaner.fonecheck.navigation

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticDestinationTest {
    @Test
    fun implementedDestinationsFollowCanonicalOrderAndIncludeThermalAndStorage() {
        assertEquals(DiagnosticCatalog.categories, diagnosticDestinations.map { it.category })
        assertEquals(ThermalTest, diagnosticDestinations.single { it.category == DiagnosticCategoryId.THERMAL }.route)
        assertEquals(StorageTest, diagnosticDestinations.single { it.category == DiagnosticCategoryId.STORAGE }.route)
    }
}
