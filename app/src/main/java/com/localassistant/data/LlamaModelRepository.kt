package com.localassistant.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class LlamaModelRepository(
    private val context: Context,
    private val remoteDataSource: RemoteModelDataSource
) : ModelRepository {
    private val modelDir by lazy {
        File(context.cacheDir, "models/llama")
    }

    // Using a single file GGUF model for now
    private val modelName = "gemma-3n-E2B-it-Q4_0.gguf"
    private val modelUrl = "https://huggingface.co/unsloth/gemma-3n-E2B-it-GGUF/resolve/main/gemma-3n-E2B-it-Q4_0.gguf"

    override suspend fun downloadAllInSubfolderFlow(
        repoId: String,
        branch: String,
        subfolder: String
    ): Flow<DownloadProgress> {
        // This implementation downloads a single file, not a subfolder.
        // The parameters are kept for interface compatibility.
        val modelFile = File(modelDir, modelName)
        return remoteDataSource.downloadFile(modelUrl, modelFile)
    }

    override suspend fun isModelAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            val modelFile = File(modelDir, modelName)
            modelFile.exists() && modelFile.length() > 0
        }
    }

    override fun getModelDirectory(): File {
        return modelDir
    }

    override fun getModelPath(): String {
        return File(modelDir, modelName).absolutePath
    }
}
