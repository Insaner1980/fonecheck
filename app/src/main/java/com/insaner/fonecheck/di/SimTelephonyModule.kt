package com.insaner.fonecheck.di

import com.insaner.fonecheck.ui.screens.simtelephony.AndroidSimTelephonyProvider
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SimTelephonyModule {
    @Provides
    @Singleton
    fun provideSimTelephonyProvider(implementation: AndroidSimTelephonyProvider): SimTelephonyProvider = implementation
}
