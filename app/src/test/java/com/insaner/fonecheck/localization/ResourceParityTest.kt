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
    private val translatedDirectories =
        listOf(
            "values-fi",
            "values-es",
            "values-pt-rBR",
            "values-de",
            "values-fr",
            "values-in",
            "values-sv",
            "values-nb",
            "values-da",
            "values-it",
            "values-pl",
            "values-tr",
        )

    @Test
    fun `registered locales match the existing picker and only app name is nontranslatable`() {
        val root = locateResourceRoot()
        val document =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(root, "xml/locales_config.xml"))
        val registered = childElements(document.documentElement).map { it.getAttribute("android:name") }
        assertEquals(AppLanguage.entries.filter { it != AppLanguage.SYSTEM }.map { it.languageTag }, registered)
        val source =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(root, "values/strings.xml"))
        val exceptions = childElements(source.documentElement).filter { it.getAttribute("translatable") == "false" }
        assertEquals(listOf("app_name"), exceptions.map { it.getAttribute("name") })
        assertEquals("fonecheck", exceptions.single().textContent)
        translatedDirectories.forEach { directory ->
            File(root, directory).listFiles().orEmpty().filter { it.extension == "xml" }.forEach { file ->
                val translated = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                assertTrue(
                    directory,
                    childElements(translated.documentElement).none { it.getAttribute("translatable") == "false" },
                )
            }
        }
        val portugueseDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-pt") || it.name.startsWith("values-b+pt"))
            }
        assertEquals(listOf("values-pt-rBR"), portugueseDirectories.map { it.name })
        val germanDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-de") || it.name.startsWith("values-b+de"))
            }
        assertEquals(listOf("values-de"), germanDirectories.map { it.name })
        val frenchDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-fr") || it.name.startsWith("values-b+fr"))
            }
        assertEquals(listOf("values-fr"), frenchDirectories.map { it.name })
        val indonesianDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && Regex("values-(?:in|id|b\\+id|b\\+in)(?:$|[-+]).*").matches(it.name)
            }
        assertEquals(listOf("values-in"), indonesianDirectories.map { it.name })
        val swedishDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-sv") || it.name.startsWith("values-b+sv"))
            }
        assertEquals(listOf("values-sv"), swedishDirectories.map { it.name })
        val norwegianDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && Regex("values-(?:b\\+)?(?:nb|no|nn)(?:$|[-+]).*").matches(it.name)
            }
        assertEquals(listOf("values-nb"), norwegianDirectories.map { it.name })
        val danishDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-da") || it.name.startsWith("values-b+da"))
            }
        assertEquals(listOf("values-da"), danishDirectories.map { it.name })
        val italianDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-it") || it.name.startsWith("values-b+it"))
            }
        assertEquals(listOf("values-it"), italianDirectories.map { it.name })
        val polishDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-pl") || it.name.startsWith("values-b+pl"))
            }
        assertEquals(listOf("values-pl"), polishDirectories.map { it.name })
        val turkishDirectories =
            root.listFiles().orEmpty().filter {
                it.isDirectory && (it.name.startsWith("values-tr") || it.name.startsWith("values-b+tr"))
            }
        assertEquals(listOf("values-tr"), turkishDirectories.map { it.name })
    }

    @Test
    fun `all shipped languages have matching translatable resource keys`() {
        val resourceRoot = locateResourceRoot()
        val english = resourceKeys(File(resourceRoot, "values/strings.xml"))
        translatedDirectories.forEach { directory ->
            assertEquals(directory, english, resourceKeys(File(resourceRoot, "$directory/strings.xml")))
        }
    }

    @Test
    fun `category names have glyphs in the shipped status panel font in every locale`() {
        val resourceRoot = locateResourceRoot()
        // Width and font-scale behavior are measured by HomeContentTest on Android.
        val font =
            Font.createFont(Font.TRUETYPE_FONT, File(resourceRoot, "font/jetbrains_mono_medium.ttf")).deriveFont(12f)

        listOf(
            "values" to Locale.ENGLISH,
            "values-fi" to Locale.forLanguageTag("fi"),
            "values-es" to Locale.forLanguageTag("es"),
            "values-pt-rBR" to Locale.forLanguageTag("pt-BR"),
            "values-de" to Locale.GERMAN,
            "values-fr" to Locale.FRENCH,
            "values-in" to Locale.forLanguageTag("id"),
            "values-sv" to Locale.forLanguageTag("sv"),
            "values-nb" to Locale.forLanguageTag("nb"),
            "values-da" to Locale.forLanguageTag("da"),
            "values-it" to Locale.ITALIAN,
            "values-pl" to Locale.forLanguageTag("pl"),
            "values-tr" to Locale.forLanguageTag("tr"),
        ).forEach { (directory, locale) ->
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

    @Test
    fun `translations preserve formatting markup and plural contracts`() {
        val root = locateResourceRoot()
        val source = textResources(File(root, "values"))
        translatedDirectories.forEach { directory ->
            val translated = textResources(File(root, directory))
            assertEquals(directory, source.keys, translated.keys)
            translated.forEach { (key, resource) ->
                val original = source.getValue(key)
                assertEquals(key, original.tagName, resource.tagName)
                val originalItems = childElements(original)
                val items = childElements(resource)
                if (resource.tagName == "plurals") {
                    val quantities = items.map { it.getAttribute("quantity") }
                    assertEquals(key, quantities.size, quantities.toSet().size)
                    val required =
                        if (directory == "values-in") {
                            setOf("other")
                        } else if (directory == "values-pl") {
                            setOf("one", "few", "many", "other")
                        } else if (directory in setOf("values-es", "values-pt-rBR", "values-fr", "values-it")) {
                            setOf("one", "many", "other")
                        } else {
                            setOf("one", "other")
                        }
                    assertEquals(key, required, quantities.toSet())
                }
                if (resource.tagName == "string-array") assertEquals(key, originalItems.size, items.size)
                val pairs =
                    when (resource.tagName) {
                        "plurals" ->
                            items.map { item ->
                                val quantity = item.getAttribute("quantity")
                                val reference =
                                    originalItems.firstOrNull { it.getAttribute("quantity") == quantity }
                                        ?: originalItems.single { it.getAttribute("quantity") == "other" }
                                reference to item
                            }
                        "string-array" -> originalItems.zip(items)
                        else -> listOf(original to resource)
                    }
                pairs.forEach { (reference, item) ->
                    assertTrue("$directory:$key is empty", item.textContent.isNotBlank())
                    assertEquals("$directory:$key placeholders", placeholders(reference), placeholders(item))
                    assertEquals("$directory:$key markup", markup(reference), markup(item))
                    assertEquals("$directory:$key line breaks", lineBreaks(reference), lineBreaks(item))
                    if (directory != "values-fi") {
                        assertTrue(key, item.textContent.none { it in "\u2013\u2014\u00b7\u2022\u2219\u22c5\u2027" })
                    }
                }
            }
        }
    }

    @Test
    fun `German text and uppercase German letters have glyphs in shipped text fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-de",
            sampleName = "uppercase German letters",
            sample = "äöüß",
            locale = Locale.GERMAN,
        )
    }

    @Test
    fun `French text punctuation and uppercase accents have glyphs in shipped fonts`() {
        val root = locateResourceRoot()
        val strings = textResources(File(root, "values-fr")).values.map { it.textContent }
        shippedTextFonts(root).forEach { (name, font) ->
            assertEquals(
                "$name French accents",
                -1,
                font.canDisplayUpTo("éèêàâçœùûîïëÿ’\u00a0".uppercase(Locale.FRENCH)),
            )
            strings.forEach { value ->
                val text = value.replace("\\n", " ").replace("\\u00a0", "\u00a0").filterNot(Char::isISOControl)
                assertEquals("$name: $text", -1, font.canDisplayUpTo(text))
                assertTrue(
                    "Use a nonbreaking space before French punctuation",
                    !Regex(" [;:?!]").containsMatchIn(text),
                )
            }
        }
    }

    @Test
    fun `Swedish text and uppercase letters have glyphs in shipped fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-sv",
            sampleName = "Swedish letters",
            sample = "åäö",
            locale = Locale.forLanguageTag("sv"),
        )
    }

    @Test
    fun `Bokmal text and uppercase letters have glyphs in shipped fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-nb",
            sampleName = "Norwegian letters",
            sample = "æøå",
            locale = Locale.forLanguageTag("nb"),
        )
    }

    @Test
    fun `Danish text and uppercase letters have glyphs in shipped fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-da",
            sampleName = "Danish letters",
            sample = "æøå",
            locale = Locale.forLanguageTag("da"),
        )
    }

    @Test
    fun `Italian accents apostrophes and uppercase letters have glyphs in shipped fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-it",
            sampleName = "Italian accents and apostrophes",
            sample = "àèéìòù’'",
            locale = Locale.ITALIAN,
        )
    }

    @Test
    fun `Polish text and uppercase letters have glyphs in shipped fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-pl",
            sampleName = "Polish letters",
            sample = "ąćęłńóśźż",
            locale = Locale.forLanguageTag("pl"),
        )
    }

    @Test
    fun `Turkish text and dotted and dotless I have glyphs in shipped fonts`() {
        assertLocalizedTextGlyphs(
            directory = "values-tr",
            sampleName = "Turkish letters",
            sample = "çğıİöşü",
            locale = Locale.forLanguageTag("tr"),
        )
    }

    private fun assertLocalizedTextGlyphs(
        directory: String,
        sampleName: String,
        sample: String,
        locale: Locale,
    ) {
        val root = locateResourceRoot()
        val strings = textResources(File(root, directory)).values.map { it.textContent }
        shippedTextFonts(root).forEach { (name, font) ->
            assertEquals("$name $sampleName", -1, font.canDisplayUpTo(sample.uppercase(locale)))
            strings.forEach { value ->
                val text = value.replace("\\n", " ").filterNot(Char::isISOControl)
                assertEquals("$name: $text", -1, font.canDisplayUpTo(text))
            }
        }
    }

    private fun shippedTextFonts(root: File): Map<String, Font> =
        listOf("dm_sans_regular", "dm_sans_medium", "jetbrains_mono_regular", "jetbrains_mono_medium")
            .associateWith { name -> Font.createFont(Font.TRUETYPE_FONT, File(root, "font/$name.ttf")) }

    private fun textResources(directory: File): Map<String, Element> =
        buildMap {
            directory.listFiles().orEmpty().filter { it.extension == "xml" }.forEach { file ->
                val root =
                    DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(file)
                        .documentElement
                childElements(root)
                    .filter {
                        it.tagName in setOf("string", "plurals", "string-array") &&
                            it.getAttribute("translatable") != "false"
                    }.forEach { element ->
                        val key = element.getAttribute("name")
                        assertTrue("Duplicate resource: $directory:$key", put(key, element) == null)
                    }
            }
        }

    private fun childElements(element: Element): List<Element> =
        (0 until element.childNodes.length).mapNotNull { element.childNodes.item(it) as? Element }

    private fun placeholders(element: Element): List<String> =
        Regex("%(?:[0-9]+\\$)?[-#+ 0,(]*[0-9]*(?:\\.[0-9]+)?(?:[tT][a-zA-Z]|[bBhHsScCdoxXeEfgGaA%n])")
            .findAll(element.textContent)
            .map { it.value }
            .sorted()
            .toList()

    private fun lineBreaks(element: Element): Int = Regex("\\\\n").findAll(element.textContent).count()

    private fun markup(element: Element): List<String> =
        childElements(element).flatMap { child ->
            val attributes = (0 until child.attributes.length).map { child.attributes.item(it).toString() }.sorted()
            listOf("${child.tagName}:$attributes") + markup(child)
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
