package com.insaner.fonecheck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ReportEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class FonecheckDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}
