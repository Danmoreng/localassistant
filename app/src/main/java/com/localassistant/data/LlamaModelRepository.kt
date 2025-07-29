package com.localassistant.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class LlamaModelRepository(private val context: Context, private val downloader: ModelDownloader) : ModelRepository {
    override suspend fun isModelAvailable(): Boolean {
        return getModelPath().isNotEmpty() && File(getModelPath()).exists()
    }

    override fun getModelPath(): String {
        val file = File(context.filesDir, "phi-2.Q4_K_M.gguf")
        return file.absolutePath
    }

    override fun getModelDirectory(): File {
        return context.filesDir
    }

    override suspend fun downloadAllInSubfolderFlow(
        repoId: String,
        branch: String,
        subfolder: String
    ): Flow<DownloadProgress> = flow {
        val url = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf"
        downloader.downloadFile(url, File(getModelPath())).collect { progress ->
            emit(progress)
        }
    }
}