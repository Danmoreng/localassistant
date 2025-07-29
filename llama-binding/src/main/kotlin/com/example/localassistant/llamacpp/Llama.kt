package com.example.localassistant.llamacpp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

object Llama {
    init {
        System.loadLibrary("llamacpp_jni")
        init()
    }

    suspend fun generate(prompt: String, modelPath: String, maxTokens: Int): Flow<String> = withContext(Dispatchers.IO) {
        flow {
            val contextPtr = newContext(modelPath)
            if (contextPtr == 0L) {
                throw IllegalStateException("Failed to create llama context")
            }

            try {
                val tokens = tokenize(prompt, true)
                evalTokens(contextPtr, tokens)

                for (i in 0 until maxTokens) {
                    val nextToken = sample(contextPtr)
                    if (nextToken == tokenEOS()) {
                        break
                    }
                    emit(tokenToPiece(contextPtr, nextToken))
                    evalTokens(contextPtr, intArrayOf(nextToken))
                }
            } finally {
                freeContext(contextPtr)
            }
        }
    }

    private external fun init()
    private external fun newContext(modelPath: String): Long
    private external fun freeContext(contextPtr: Long)
    private external fun evalTokens(contextPtr: Long, tokenIds: IntArray): Int
    private external fun sample(contextPtr: Long): Int
    private external fun tokenToPiece(contextPtr: Long, tokenId: Int): String
    private external fun tokenEOS(): Int
    private external fun tokenize(text: String, addBos: Boolean): IntArray
}