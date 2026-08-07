package com.insaner.fonecheck.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FonecheckDatabaseSchemaTest {
    @get:Rule
    val migrationHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            FonecheckDatabase::class.java,
        )

    @Test
    fun exportedVersionOneSchemaCreatesTheReportsTable() {
        migrationHelper.createDatabase(TEST_DATABASE, 1).use { database ->
            database.query("PRAGMA table_info(`reports`)").use { cursor ->
                val nameColumn = cursor.getColumnIndexOrThrow("name")
                val columnNames = buildSet {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(nameColumn))
                    }
                }

                assertEquals(EXPECTED_REPORT_COLUMNS, columnNames)
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "task-4-schema-test"

        val EXPECTED_REPORT_COLUMNS =
            setOf(
                "id",
                "reportKindCode",
                "categoryId",
                "startedAtEpochMillis",
                "completedAtEpochMillis",
                "reportSchemaVersion",
                "scoreVersion",
                "scoreValue",
                "scoreStateCode",
                "coveragePercentage",
                "applicableCount",
                "completedCount",
                "notTestedCount",
                "unavailableCount",
                "warningCount",
                "failureCount",
                "payloadJson",
            )
    }
}
