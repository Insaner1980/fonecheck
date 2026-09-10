package com.insaner.fonecheck.localization

import com.insaner.fonecheck.data.repository.enumFromStableCodeOrNull
import com.insaner.fonecheck.data.repository.stableCode
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TurkishCasingTest {
    @Test
    fun dottedAndDotlessIUseTurkishPresentationRules() {
        listOf("tr", "tr-TR").forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            assertEquals("İZİN VE IŞIK", "İzin ve ışık".uppercase(locale))
            assertEquals("izin ve ışık", "İZİN VE IŞIK".lowercase(locale))
            assertEquals("İ I İ I", "i ı İ I".uppercase(locale))
            assertEquals("i ı i ı", "i ı İ I".lowercase(locale))
        }
    }

    @Test
    fun storedCodesKeepInvariantIUnderTurkishDefaultLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("info", DiagnosticStatus.INFO.stableCode())
            assertEquals("fail", DiagnosticStatus.FAIL.stableCode())
            assertEquals(DiagnosticStatus.INFO, enumFromStableCodeOrNull<DiagnosticStatus>("info"))
            assertEquals("Future info", stableCodeDisplayText("future_info"))
        } finally {
            Locale.setDefault(original)
        }
    }
}
