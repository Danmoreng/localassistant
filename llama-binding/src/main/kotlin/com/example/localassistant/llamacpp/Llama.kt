package com.example.localassistant.llamacpp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

class Llama(
    private val modelPath: String
) {
    init {
        System.loadLibrary("llamacpp_jni")
    }

    fun release() {
        // No-op, as context is released after each generation
    }

    suspend fun generate(prompt: String, maxTokens: Int): Flow<String> = withContext(Dispatchers.IO) {
        flow {
            val contextPtr = newContext(modelPath)
            if (contextPtr == 0L) {
                throw IllegalStateException("Failed to create llama context")
            }

            try {
                val tokens = tokenize(contextPtr, prompt, true)
                if (tokens.isEmpty()) {
                    return@flow
                }
                evalTokens(contextPtr, tokens)

                for (i in 0 until maxTokens) {
                    val next = sample(contextPtr)
                    if (next < 0 || next == tokenEOS(contextPtr)) break
                    val bytes = tokenToPiece(contextPtr, next)
                    if (bytes.isNotEmpty()) {
                        emit(String(bytes, StandardCharsets.UTF_8))
                    }
                    evalTokens(contextPtr, intArrayOf(next))
                }
            } finally {
                freeContext(contextPtr)
            }
        }
    }

    private external fun newContext(modelPath: String): Long
    private external fun freeContext(contextPtr: Long)
    private external fun evalTokens(contextPtr: Long, tokenIds: IntArray): Int
    private external fun sample(contextPtr: Long): Int
    private external fun tokenToPiece(contextPtr: Long, tokenId: Int): ByteArray
    private external fun tokenEOS(contextPtr: Long): Int
    private external fun tokenize(contextPtr: Long, text: String, addBos: Boolean): IntArray
}