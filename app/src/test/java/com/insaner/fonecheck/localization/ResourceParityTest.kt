package com.insaner.fonecheck.localization

import com.insaner.fonecheck.navigation.diagnosticDestinations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.awt.Font
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

class ResourceParityTest {
    @Test
    fun `English and Finnish translatable resources have matching keys`() {
        val resourceRoot = locateResourceRoot()
        val english = resourceKeys(File(resourceRoot, "values/strings.xml"))
        val finnish = resourceKeys(File(resourceRoot, "values-fi/strings.xml"))

        assertEquals(english, finnish)
    }

    @Test
    fun `category names have glyphs in the shipped status panel font in every locale`() {
        val resourceRoot = locateResourceRoot()
        // Width and font-scale behavior are measured by HomeContentTest on Android.
        val font =
            Font.createFont(Font.TRUETYPE_FONT, File(resourceRoot, "font/jetbrains_mono_medium.ttf")).deriveFont(12f)

        listOf("values" to Locale.ENGLISH, "values-fi" to Locale.forLanguageTag("fi")).forEach { (directory, locale) ->
            val file = File(resourceRoot, "$directory/strings.xml")
            val keys = resourceKeys(file).filter { it.startsWith("string:home_cat_") }
            assertEquals("$directory must cover every category", diagnosticDestinations.size, keys.size)
            keys.forEach { key ->
                val name = stringValue(file, key.removePrefix("string:")).uppercase(locale)
                assertTrue("$directory: $name needs glyphs in the shipped font", font.canDisplayUpTo(name) == -1)
            }
        }
    }

    @Test
    fun `Full Check vocabulary is locked in English and Finnish`() {
        val resourceRoot = locateResourceRoot()
        val english = File(resourceRoot, "values/strings.xml")
        val finnish = File(resourceRoot, "values-fi/strings.xml")

        assertEquals("Full Check", stringValue(english, "full_check_title"))
        assertEquals("Full Check", stringValue(finnish, "full_check_title"))
        assertEquals("Start Full Check", stringValue(english, "home_start_full_check"))
        assertEquals("Aloita Full Check", stringValue(finnish, "home_start_full_check"))
        assertEquals("Active modems", stringValue(english, "label_active_modem_count"))
        assertEquals("Aktiiviset modeemit", stringValue(finnish, "label_active_modem_count"))
    }

    private fun locateResourceRoot(): File {
        val workingDirectory = File(requireNotNull(System.getProperty("user.dir")))
        val candidates =
            listOf(
                File(workingDirectory, "src/main/res"),
                File(workingDirectory, "app/src/main/res"),
            )
        return requireNotNull(candidates.firstOrNull(File::isDirectory)) {
            "Android resource directory was not found from $workingDirectory"
        }
    }

    private fun resourceKeys(file: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.documentElement.childNodes
        return buildSet {
            for (index in 0 until nodes.length) {
                val element = nodes.item(index) as? Element
                if (element != null && element.getAttribute("translatable") != "false") {
                    val name = element.getAttribute("name")
                    if (name.isNotBlank()) add("${element.tagName}:$name")
                }
            }
        }
    }

    private fun stringValue(
        file: File,
        key: String,
    ): String {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as Element
            if (element.getAttribute("name") == key) return element.textContent
        }
        error("String resource $key was not found in $file")
    }
}
