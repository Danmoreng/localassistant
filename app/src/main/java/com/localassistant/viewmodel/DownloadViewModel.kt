package com.localassistant.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localassistant.data.DownloadProgress
import com.localassistant.data.LlamaModelRepository
import com.localassistant.data.ModelDownloader
import com.localassistant.data.ModelRepository
import com.localassistant.data.Phi4ModelRepository
import com.localassistant.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class DownloadUiState(
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val currentFileName: String = ""
)

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val app: Application,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private lateinit var repository: ModelRepository

    var uiState by mutableStateOf(DownloadUiState())
        private set

    var isModelAvailable by mutableStateOf(false)
        private set

    init {
        runBlocking {
            val engine = settingsRepository.selectedEngine.first()
            initializeRepository(engine)
        }
        viewModelScope.launch {
            isModelAvailable = repository.isModelAvailable()
        }
    }

    private fun initializeRepository(engine: String) {
        repository = when (engine) {
            "phi" -> Phi4ModelRepository(app, ModelDownloader())
            "llama" -> LlamaModelRepository(app, ModelDownloader())
            else -> throw IllegalArgumentException("Unknown engine: $engine")
        }
    }

    fun checkModelAvailability() {
        viewModelScope.launch {
            isModelAvailable = repository.isModelAvailable()
        }
    }

    fun resetRepository() {
        runBlocking {
            val engine = settingsRepository.selectedEngine.first()
            initializeRepository(engine)
        }
        checkModelAvailability()
    }

    fun downloadModel() {
        uiState = uiState.copy(isDownloading = true, errorMessage = null)

        viewModelScope.launch {
            val repoId = if (settingsRepository.selectedEngine.first() == "phi") {
                "microsoft/Phi-4-mini-instruct-onnx"
            } else {
                "ggml-org/gemma-2b-it-GGUF"
            }
            val subfolder = if (settingsRepository.selectedEngine.first() == "phi") {
                "cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4"
            } else {
                ""
            }

            repository.downloadAllInSubfolderFlow(
                repoId = repoId,
                branch = "main",
                subfolder = subfolder
            ).collect { progress ->
                when (progress) {
                    is DownloadProgress.ListingFiles -> {
                        uiState = uiState.copy(
                            totalFiles = progress.totalFiles,
                            currentFileIndex = 0,
                            currentFileName = ""
                        )
                    }
                    is DownloadProgress.DownloadingFile -> {
                        uiState = uiState.copy(
                            currentFileIndex = progress.currentIndex,
                            totalFiles = progress.totalFiles,
                            currentFileName = progress.fileName
                        )
                    }
                    is DownloadProgress.Success -> {
                        uiState = uiState.copy(isDownloading = false)
                        isModelAvailable = true
                    }
                    is DownloadProgress.Error -> {
                        uiState = uiState.copy(
                            isDownloading = false,
                            errorMessage = progress.message
                        )
                    }
                }
            }
        }
    }
}

