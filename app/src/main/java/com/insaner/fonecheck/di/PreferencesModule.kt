package com.insaner.fonecheck.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.data.preferences.DataStoreAppPreferencesRepository
import com.insaner.fonecheck.ui.screens.settings.AndroidSettingsPermissionProvider
import com.insaner.fonecheck.ui.screens.settings.SettingsPermissionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            val preferencesFile = context.preferencesDataStoreFile("fonecheck")
            val legacyFile = context.preferencesDataStoreFile("fonecheck.preferences_pb")
            if (!preferencesFile.exists() && legacyFile.exists()) {
                check(legacyFile.renameTo(preferencesFile)) { "Could not migrate preferences DataStore." }
            }
            preferencesFile
        }

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(dataStore: DataStore<Preferences>): AppPreferencesRepository =
        DataStoreAppPreferencesRepository(dataStore)

    @Provides
    fun provideSettingsPermissionProvider(provider: AndroidSettingsPermissionProvider): SettingsPermissionProvider =
        provider
}
