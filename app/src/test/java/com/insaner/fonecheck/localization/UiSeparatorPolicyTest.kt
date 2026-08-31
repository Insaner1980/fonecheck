package com.insaner.fonecheck.localization

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class UiSeparatorPolicyTest {
    @Test
    fun `production sources do not use forbidden separators`() {
        val sourceRoot = locateSourceRoot()
        val matches =
            sourceRoot
                .walkTopDown()
                .filter(File::isFile)
                .filter { it.extension in sourceExtensions }
                .sortedBy { it.relativeTo(sourceRoot).invariantSeparatorsPath }
                .flatMap { file -> forbiddenSeparatorMatches(file, sourceRoot).asSequence() }
                .toList()

        assertTrue(
            "Forbidden separators found:\n${matches.joinToString("\n")}",
            matches.isEmpty(),
        )
    }

    private fun locateSourceRoot(): File {
        val workingDirectory = File(requireNotNull(System.getProperty("user.dir")))
        val candidates =
            listOf(
                File(workingDirectory, "src/main"),
                File(workingDirectory, "app/src/main"),
            )
        return requireNotNull(candidates.firstOrNull(File::isDirectory)) {
            "Android production source directory was not found from $workingDirectory"
        }
    }

    private fun forbiddenSeparatorMatches(
        file: File,
        sourceRoot: File,
    ): List<String> {
        val source = file.readText()
        val matches = mutableListOf<String>()
        var offset = 0
        var line = 1
        while (offset < source.length) {
            val codePoint = source.codePointAt(offset)
            if (codePoint in forbiddenCodePoints) {
                val relativePath = file.relativeTo(sourceRoot).invariantSeparatorsPath
                matches += "$relativePath:$line:U+${"%04X".format(Locale.ROOT, codePoint)}"
            }
            if (codePoint == NEWLINE_CODE_POINT) line++
            offset += Character.charCount(codePoint)
        }
        return matches
    }

    private companion object {
        val sourceExtensions = setOf("kt", "xml")
        val forbiddenCodePoints = setOf(0x00B7, 0x2022, 0x2219, 0x22C5, 0x2027)
        const val NEWLINE_CODE_POINT = 0x000A
    }
}
