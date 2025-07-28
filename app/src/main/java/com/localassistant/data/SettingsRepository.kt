package com.localassistant.data

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val selectedEngine: Flow<String>
    suspend fun setSelectedEngine(engine: String)
}
