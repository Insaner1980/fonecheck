package com.insaner.fonecheck.localization

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

class ResourceParityTest {
    @Test
    fun `English and Finnish translatable resources have matching keys`() {
        val resourceRoot = locateResourceRoot()
        val english = resourceKeys(File(resourceRoot, "values/strings.xml"))
        val finnish = resourceKeys(File(resourceRoot, "values-fi/strings.xml"))

        assertEquals(english, finnish)
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
                val element = nodes.item(index) as? Element ?: continue
                if (element.getAttribute("translatable") == "false") continue
                val name = element.getAttribute("name")
                if (name.isNotBlank()) add("${element.tagName}:$name")
            }
        }
    }
}
