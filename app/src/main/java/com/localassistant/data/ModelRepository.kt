package com.localassistant.data

import kotlinx.coroutines.flow.Flow
import java.io.File

interface ModelRepository {
    suspend fun downloadAllInSubfolderFlow(
        repoId: String,
        branch: String,
        subfolder: String
    ): Flow<DownloadProgress>

    suspend fun isModelAvailable(): Boolean

    fun getModelDirectory(): File
}
