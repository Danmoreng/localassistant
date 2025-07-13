package com.localassistant.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localassistant.data.DownloadProgress
import com.localassistant.data.ModelRepository
import kotlinx.coroutines.launch

/**
 * The UI state for the download screen.
 */
data class DownloadUiState(
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val currentFileName: String = ""
)

class DownloadViewModel(
    application: Application,
    private val repository: ModelRepository
) : AndroidViewModel(application) {

    var uiState by mutableStateOf(DownloadUiState())
        private set

    // Exposed state for model availability.
    var isModelAvailable by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Update the state with the availability of the model.
            isModelAvailable = repository.isModelAvailable()
        }
    }

    fun downloadModel() {

        // Update UI to "downloading" state
        uiState = uiState.copy(isDownloading = true, errorMessage = null)

        viewModelScope.launch {
            repository.downloadAllInSubfolderFlow(
                repoId = "microsoft/Phi-4-mini-instruct-onnx",
                branch = "main",
                subfolder = "cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4"
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
                        // Mark as done
                        uiState = uiState.copy(isDownloading = false)
                        isModelAvailable = true
                    }
                    is DownloadProgress.Error -> {
                        // Show error
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
