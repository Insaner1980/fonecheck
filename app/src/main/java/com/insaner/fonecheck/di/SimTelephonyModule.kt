package com.insaner.fonecheck.di

import com.insaner.fonecheck.ui.screens.simtelephony.AndroidSimTelephonyProvider
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SimTelephonyModule {
    @Binds
    @Singleton
    abstract fun bindSimTelephonyProvider(
        implementation: AndroidSimTelephonyProvider,
    ): SimTelephonyProvider
}
