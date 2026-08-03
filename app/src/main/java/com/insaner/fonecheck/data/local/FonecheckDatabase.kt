package com.insaner.fonecheck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FonecheckDatabase : RoomDatabase()
