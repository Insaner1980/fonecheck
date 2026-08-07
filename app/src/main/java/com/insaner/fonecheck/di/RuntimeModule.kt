package com.insaner.fonecheck.di

import android.content.Context
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.runtime.NanoTimeSource
import com.insaner.fonecheck.ui.screens.storage.AndroidStorageBenchmarkStore
import com.insaner.fonecheck.ui.screens.storage.AndroidStorageInfoProvider
import com.insaner.fonecheck.ui.screens.storage.DefaultStorageBenchmarkRunner
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkRunner
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkStore
import com.insaner.fonecheck.ui.screens.storage.StorageInfoProvider
import com.insaner.fonecheck.ui.screens.thermal.AndroidThermalPlatform
import com.insaner.fonecheck.ui.screens.thermal.ThermalPlatform
import com.insaner.fonecheck.ui.screens.vibration.AndroidVibrationPlatform
import com.insaner.fonecheck.ui.screens.vibration.VibrationPlatform
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
    fun provideStorageInfoProvider(provider: AndroidStorageInfoProvider): StorageInfoProvider = provider

    @Provides
    fun provideStorageBenchmarkStore(store: AndroidStorageBenchmarkStore): StorageBenchmarkStore = store

    @Provides
    fun provideStorageBenchmarkRunner(runner: DefaultStorageBenchmarkRunner): StorageBenchmarkRunner = runner

    @Provides
    fun provideVibrationPlatform(platform: AndroidVibrationPlatform): VibrationPlatform = platform

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
