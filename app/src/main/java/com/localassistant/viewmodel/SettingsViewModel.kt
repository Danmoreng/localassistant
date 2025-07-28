package com.localassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localassistant.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedEngine = settingsRepository.selectedEngine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "phi")

    fun setSelectedEngine(engine: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedEngine(engine)
        }
    }
}
