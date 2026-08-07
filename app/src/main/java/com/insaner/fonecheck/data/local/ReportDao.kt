package com.insaner.fonecheck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: ReportEntity)

    @Query(
        """
        SELECT id, reportKindCode, categoryId, completedAtEpochMillis,
            reportSchemaVersion, scoreVersion, scoreValue, scoreStateCode, coveragePercentage,
            warningCount, failureCount
        FROM reports
        ORDER BY completedAtEpochMillis DESC
        """,
    )
    fun observeSummaries(): Flow<List<ReportSummary>>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getById(id: String): ReportEntity?

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reports")
    suspend fun deleteAll()
}
