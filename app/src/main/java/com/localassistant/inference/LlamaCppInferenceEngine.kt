package com.localassistant.inference

import android.util.Log
import com.example.localassistant.llamacpp.Llama
import com.localassistant.engine.InferenceEngine
import com.localassistant.model.Message
import com.localassistant.model.TextMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LlamaCppInferenceEngine(
    private val modelPath: String
) : InferenceEngine {

    private var llama: Llama? = null

    init {
        Log.d("LlamaCppInferenceEngine", "LlamaCppInferenceEngine initialized with model path: $modelPath")
    }

    override suspend fun load() {
        llama = Llama(modelPath)
    }

    override suspend fun generateResponse(prompt: String): Flow<String> {
        Log.d("LlamaCppInferenceEngine", "Formatted Prompt: $prompt")
        return llama?.generate(prompt, 256) ?: flow {
            emit("Error: Llama not initialized")
        }
    }

    override fun formatChat(messages: List<Message>, systemPrompt: String): String {
        val sb = StringBuilder()
        if (systemPrompt.isNotBlank()) {
            sb.append("<|im_start|>system\n$systemPrompt<|im_end|>\n")
        }
        messages.reversed().forEach { message ->
            if (message is TextMessage) {
                sb.append("<|im_start|>${message.type.name.toLowerCase()}\n${message.text}<|im_end|>\n")
            }
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    override suspend fun close() {
        llama?.release()
        llama = null
    }
}