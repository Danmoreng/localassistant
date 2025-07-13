package com.localassistant.data

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Abstraction for any remote data source that provides model files.
 */
interface RemoteModelDataSource {

    /**
     * Lists all files in a Hugging Face repo (branch/revision).
     */
    @Throws(Exception::class)
    suspend fun listRepoFiles(repoId: String, revision: String = "main"): List<ModelDownloader.HubFileEntry>

    /**
     * Downloads a subfolder from Hugging Face as a stream of progress updates.
     * Emitted values can represent:
     * - Stage: listing files
     * - Stage: downloading file #n
     * - Success / Error
     */
    @OptIn(UnstableApi::class)
    @Throws(Exception::class)
    suspend fun downloadSubfolderFlow(
        repoId: String,
        branch: String = "main",
        subfolder: String,
        destDir: File
    ): Flow<com.localassistant.data.DownloadProgress>

    suspend fun downloadFile(
        url: String,
        destFile: File
    ): Flow<com.localassistant.data.DownloadProgress>
}
