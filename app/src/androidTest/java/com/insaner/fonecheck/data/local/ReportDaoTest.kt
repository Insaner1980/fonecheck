package com.insaner.fonecheck.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportDaoTest {
    private lateinit var database: FonecheckDatabase
    private val executedQueries = mutableListOf<String>()

    @Before
    fun setUp() {
        executedQueries.clear()
        database =
            Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                FonecheckDatabase::class.java,
            ).setQueryCallback(
                { sqlQuery, _ -> executedQueries += sqlQuery },
                Executor { command -> command.run() },
            ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadReturnsTheFullImmutableReport() =
        runBlocking {
            val report = report(id = "report-1", completedAt = 2_000L, payload = "{\"id\":1}")

            database.reportDao().insert(report)

            assertEquals(report, database.reportDao().getById("report-1"))
        }

    @Test
    fun summariesAreNewestFirstAndExcludeThePayload() =
        runBlocking {
            database.reportDao().insert(report(id = "older", completedAt = 2_000L, payload = "older-payload"))
            database.reportDao().insert(report(id = "newer", completedAt = 3_000L, payload = "newer-payload"))

            val summaries = database.reportDao().observeSummaries().first()

            assertEquals(
                listOf(
                    summary(id = "newer", completedAt = 3_000L),
                    summary(id = "older", completedAt = 2_000L),
                ),
                summaries,
            )
            val summarySelect =
                executedQueries.single {
                    it.contains("ORDER BY completedAtEpochMillis DESC", ignoreCase = true)
                }
            assertFalse(summarySelect.contains("payloadJson", ignoreCase = true))
        }

    private fun summary(
        id: String,
        completedAt: Long,
    ) =
        ReportSummary(
            id = id,
            reportKindCode = "full_check",
            categoryId = null,
            completedAtEpochMillis = completedAt,
            scoreVersion = 1,
            scoreValue = 90,
            scoreStateCode = "complete",
            coveragePercentage = 100,
            warningCount = 1,
            failureCount = 0,
        )

    private fun report(
        id: String,
        completedAt: Long,
        payload: String,
    ) =
        ReportEntity(
            id = id,
            reportKindCode = "full_check",
            categoryId = null,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = completedAt,
            reportSchemaVersion = 1,
            scoreVersion = 1,
            scoreValue = 90,
            scoreStateCode = "complete",
            coveragePercentage = 100,
            applicableCount = 2,
            completedCount = 2,
            notTestedCount = 0,
            unavailableCount = 0,
            warningCount = 1,
            failureCount = 0,
            payloadJson = payload,
        )
}
