package com.localassistant.data

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException

/**
 * Concrete implementation of RemoteModelDataSource that uses OkHttp to
 * fetch the list of files from Hugging Face and download them.
 */
class ModelDownloader(
    private val client: OkHttpClient = defaultOkHttpClient
) : RemoteModelDataSource {

    // region Data Classes

    /**
     * Sibling file entry from the Hugging Face Hub API.
     */
    @kotlin.OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class HubFileEntry(
        val rfilename: String
    )

    /**
     * Top-level model info from the Hugging Face Hub API.
     */
    @kotlin.OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class HubModelInfo(
        val siblings: List<HubFileEntry>
    )

    // endregion

    override suspend fun listRepoFiles(repoId: String, revision: String): List<HubFileEntry> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://huggingface.co/api/models/$repoId?revision=$revision")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed listing repo files: ${response.code}")
                }
                val jsonResponse = response.body?.string()
                    ?: throw IOException("Empty response.")

                Json.decodeFromString<HubModelInfo>(jsonResponse).siblings
            }
        }

    /**
     * Return a Flow of [DownloadProgress], which will:
     * 1. Emit [DownloadProgress.ListingFiles] once we know how many files we'll download
     * 2. For each file, emit [DownloadProgress.DownloadingFile] before we download
     * 3. Finally emit [DownloadProgress.Success] or [DownloadProgress.Error]
     */
    @OptIn(UnstableApi::class)
    override suspend fun downloadSubfolderFlow(
        repoId: String,
        branch: String,
        subfolder: String,
        destDir: File
    ): Flow<DownloadProgress> = flow {
        // 1) List all files in the repo
        val allFiles = listRepoFiles(repoId, branch)

        // 2) Filter for our subfolder
        val subfolderFiles = allFiles
            .filter { it.rfilename.startsWith("$subfolder/") }
            .sortedBy { it.rfilename }

        // 3) Create the destination directory if it doesn’t exist
        if (!destDir.exists()) destDir.mkdirs()

        // Emit initial progress with total file count
        emit(DownloadProgress.ListingFiles(subfolderFiles.size))

        // 4) Download each file sequentially (simple approach)
        subfolderFiles.forEachIndexed { index, hubFile ->
            emit(
                DownloadProgress.DownloadingFile(
                    currentIndex = index + 1,
                    totalFiles = subfolderFiles.size,
                    fileName = hubFile.rfilename
                )
            )

            val fileUrl =
                "https://huggingface.co/$repoId/resolve/$branch/${hubFile.rfilename}"
            val targetFileName = hubFile.rfilename.removePrefix("$subfolder/")
            val localFile = File(destDir, targetFileName)

            // Download if missing or zero-sized
            if (!localFile.exists() || localFile.length() == 0L) {
                downloadSingleFile(fileUrl, localFile)
            }
        }

        // All files done
        emit(DownloadProgress.Success)
    }.catch { e ->
        Log.e("ModelDownloader", "Error downloading subfolder: ${e.message}")
        emit(DownloadProgress.Error(e.message ?: "Unknown error"))
    }

    /**
     * Downloads a single file from [fileUrl] into [destFile].
     */
    @Throws(IOException::class)
    suspend fun downloadSingleFile(fileUrl: String, destFile: File) = withContext(Dispatchers.IO) {
        Log.d("ModelDownloader", "Downloading $fileUrl to ${destFile.absolutePath}")

        val request = Request.Builder()
            .url(fileUrl)
            .header("User-Agent", "Mozilla/5.0 (compatible; LocalAssistant)")
            .build()

        client.newCall(request).execute().use { response ->
            Log.d("ModelDownloader", "Response code: ${response.code}")

            if (!response.isSuccessful) {
                val errorMsg = "Failed to download file from $fileUrl: " +
                        "${response.code} ${response.message}"
                Log.e("ModelDownloader", errorMsg)
                throw IOException(errorMsg)
            }

            val bodyStream = response.body?.byteStream()
                ?: throw IOException("Response body is null for $fileUrl")

            destFile.outputStream().use { output ->
                bodyStream.copyTo(output)
            }

            Log.d("ModelDownloader", "File downloaded: ${destFile.name}")
        }
    }

    companion object {
        private val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val defaultOkHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
}
