package com.localassistant.inference

import android.llama.cpp.LLamaAndroid
import com.localassistant.engine.InferenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class LlamaCppInferenceEngine(
    modelPath: String
) : InferenceEngine {

    private val llama = LLamaAndroid.instance()

    init {
        runBlocking {
            llama.load(modelPath)
        }
    }

    override fun generateResponse(prompt: String): Flow<String> {
        return llama.send(prompt, true)
    }
}
