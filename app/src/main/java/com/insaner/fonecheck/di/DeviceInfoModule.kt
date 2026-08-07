package com.insaner.fonecheck.di

import com.insaner.fonecheck.ui.screens.deviceinfo.AndroidDeviceInfoProvider
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceInfoModule {
    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(implementation: AndroidDeviceInfoProvider): DeviceInfoProvider
}
