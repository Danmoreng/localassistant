package com.localassistant.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository that manages local Phi-4 model files.
 */
class Phi4ModelRepository(
    context: Context,
    private val remoteDataSource: RemoteModelDataSource
) : ModelRepository {

    // Directory in internal storage for model files
    private val modelDirName = "phi4_mini_instruct_cpu_int4"
    private val modelDir: File = File(context.filesDir, modelDirName)

    /**
     * Returns a Flow of DownloadProgress for the subfolder’s files.
     */
    @OptIn(UnstableApi::class)
    override suspend fun downloadAllInSubfolderFlow(
        repoId: String,
        branch: String,
        subfolder: String
    ): Flow<DownloadProgress> {
        return remoteDataSource.downloadSubfolderFlow(repoId, branch, subfolder, modelDir)
    }

    /**
     * Checks if all files for this subfolder exist locally and are non-empty.
     * Returns true if all are found, false otherwise.
     */
    override suspend fun isModelAvailable(): Boolean {
    if (!modelDir.exists()) return false

    return try {
        // Check the same subfolder the app uses
        val repoId = "microsoft/Phi-4-mini-instruct-onnx"
        val branch = "main"
        val subfolder = "cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4"

        val allFiles = remoteDataSource.listRepoFiles(repoId, branch)
        val subfolderFiles = allFiles
            .filter { it.rfilename.startsWith("$subfolder/") }

        // Ensure each file is present locally and not zero-sized
        for (hubFile in subfolderFiles) {
            val localFileName = hubFile.rfilename.removePrefix("$subfolder/")
            val localFile = File(modelDir, localFileName)

            if (!localFile.exists() || localFile.length() == 0L) {
                return false
            }
            // Optionally also check localFile.length() == hubFile.size
        }
        true
    } catch (e: Exception) {
        false
    }
}

    override fun getModelDirectory(): File = modelDir

    override fun getModelPath(): String {
        return modelDir.absolutePath
    }
}
