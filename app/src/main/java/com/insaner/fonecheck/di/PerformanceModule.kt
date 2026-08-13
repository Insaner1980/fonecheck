package com.insaner.fonecheck.di

import com.insaner.fonecheck.ui.screens.performance.AndroidPerformanceInfoProvider
import com.insaner.fonecheck.ui.screens.performance.AndroidThermalStatusReader
import com.insaner.fonecheck.ui.screens.performance.DefaultPerformanceBenchmarkRunner
import com.insaner.fonecheck.ui.screens.performance.PerformanceBenchmarkRunner
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoProvider
import com.insaner.fonecheck.ui.screens.performance.ThermalStatusReader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PerformanceModule {
    @Binds
    @Singleton
    fun bindPerformanceInfoProvider(implementation: AndroidPerformanceInfoProvider): PerformanceInfoProvider

    @Binds
    fun bindPerformanceBenchmarkRunner(implementation: DefaultPerformanceBenchmarkRunner): PerformanceBenchmarkRunner

    @Binds
    fun bindThermalStatusReader(implementation: AndroidThermalStatusReader): ThermalStatusReader
}
