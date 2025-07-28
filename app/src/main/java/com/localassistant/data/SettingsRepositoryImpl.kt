package com.localassistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : SettingsRepository {

    private val selectedEngineKey = stringPreferencesKey("selected_engine")

    override val selectedEngine: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[selectedEngineKey] ?: "phi"
        }

    override suspend fun setSelectedEngine(engine: String) {
        context.dataStore.edit { settings ->
            settings[selectedEngineKey] = engine
        }
    }
}
