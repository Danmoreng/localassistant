package com.example.localassistant.data

import android.util.Log
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import okhttp3.logging.HttpLoggingInterceptor

object ModelDownloader {
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    @Throws(IOException::class)
    fun downloadFile(url: String, destFile: File) {
        Log.d("ModelDownloader", "Starting download from $url")
        val request = okhttp3.Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (compatible; LocalAssistant)")
            .build()

        client.newCall(request).execute().use { response ->
            // Log the response code even if the response fails
            Log.d("ModelDownloader", "Response code: ${response.code}")

            if (!response.isSuccessful) {
                val errorMsg = "Failed to download file from $url: ${response.code} ${response.message}"
                Log.e("ModelDownloader", errorMsg)
                throw IOException(errorMsg)
            }

            val bodyStream = response.body?.byteStream()
                ?: throw IOException("Response body is null for $url")

            destFile.outputStream().use { output ->
                bodyStream.copyTo(output)
            }

            Log.d("ModelDownloader", "File downloaded successfully: ${destFile.name}")
        }
    }
}
