package com.insaner.fonecheck.di

import com.insaner.fonecheck.ui.screens.deviceinfo.AndroidDeviceInfoProvider
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceInfoModule {
    @Provides
    @Singleton
    fun provideDeviceInfoProvider(implementation: AndroidDeviceInfoProvider): DeviceInfoProvider = implementation
}
