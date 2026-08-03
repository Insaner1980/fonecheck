package com.insaner.fonecheck.di

import android.content.Context
import androidx.room.Room
import com.insaner.fonecheck.data.local.FonecheckDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): FonecheckDatabase =
        Room
            .databaseBuilder(
                context,
                FonecheckDatabase::class.java,
                "fonecheck.db",
            ).build()
}
