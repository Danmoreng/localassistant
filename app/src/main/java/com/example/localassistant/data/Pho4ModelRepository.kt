package com.example.localassistant.data

import android.content.Context
import java.io.File

class Phi4ModelRepository(private val context: Context) {

    // The folder name in internal storage where we keep model files
    private val modelDirName = "phi4_mini_instruct_cpu_int4"

    // Directory object for the local model folder
    private val modelDir: File
        get() = File(context.filesDir, modelDirName)

    /**
     * Check if all required files are present in [modelDir].
     */
    fun isModelAvailable(): Boolean {
        if (!modelDir.exists()) return false
        // Ensure each required file is present and non-empty
        return Phi4MiniFiles.REQUIRED_FILES.all { fileName ->
            val file = File(modelDir, fileName)
            file.exists() && file.length() > 0
        }
    }

    /**
     * Download all required model files if they are missing.
     * This might be called after user confirmation.
     */
    @Throws(Exception::class)
    fun downloadModelFiles() {
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        Phi4MiniFiles.REQUIRED_FILES.forEach { fileName ->
            val localFile = File(modelDir, fileName)
            if (!localFile.exists() || localFile.length() == 0L) {
                val url = Phi4MiniFiles.getDownloadUrl(fileName)
                ModelDownloader.downloadFile(url, localFile)
            }
        }
    }

    /**
     * Returns the [File] path to the model directory.
     * You can pass this to your ONNX loader, etc.
     */
    fun getModelDirectory(): File {
        return modelDir
    }

    /**
     * (Optional) Helper to get a specific file path by name.
     */
    fun getFilePath(fileName: String): String {
        return File(modelDir, fileName).absolutePath
    }
}
