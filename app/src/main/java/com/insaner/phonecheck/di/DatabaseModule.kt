package com.insaner.phonecheck.di

import android.content.Context
import androidx.room.Room
import com.insaner.phonecheck.data.local.PhoneCheckDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): PhoneCheckDatabase {
        return Room.databaseBuilder(
            context,
            PhoneCheckDatabase::class.java,
            "phonecheck.db",
        ).build()
    }
}
