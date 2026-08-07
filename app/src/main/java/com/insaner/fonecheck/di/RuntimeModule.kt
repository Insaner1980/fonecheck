package com.insaner.fonecheck.di

import android.content.Context
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.runtime.NanoTimeSource
import com.insaner.fonecheck.ui.screens.thermal.AndroidThermalPlatform
import com.insaner.fonecheck.ui.screens.thermal.ThermalPlatform
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object RuntimeModule {
    @Provides
    fun provideEpochMillisClock(): EpochMillisClock =
        EpochMillisClock { System.currentTimeMillis() }

    @Provides
    fun provideIdProvider(): IdProvider =
        IdProvider { UUID.randomUUID().toString() }

    @Provides
    fun provideNanoTimeSource(): NanoTimeSource =
        NanoTimeSource(System::nanoTime)

    @Provides
    fun provideThermalPlatform(
        @ApplicationContext context: Context,
    ): ThermalPlatform = AndroidThermalPlatform(context)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
