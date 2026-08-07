package com.insaner.fonecheck.di

import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.runtime.NanoTimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
