package com.example.localassistant.data

/**
 * A sealed class to represent download progress states in a Flow.
 */
sealed class DownloadProgress {
    /**
     * Emitted once we know how many files are in the subfolder.
     */
    data class ListingFiles(val totalFiles: Int) : DownloadProgress()

    /**
     * Emitted before downloading each file.
     */
    data class DownloadingFile(
        val currentIndex: Int,
        val totalFiles: Int,
        val fileName: String
    ) : DownloadProgress()

    /**
     * Emitted once the entire subfolder’s files have downloaded successfully.
     */
    data object Success : DownloadProgress()

    /**
     * Emitted if an error occurs at any stage of listing or downloading.
     */
    data class Error(val message: String) : DownloadProgress()
}
