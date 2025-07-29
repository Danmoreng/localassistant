package com.localassistant.inference

import android.util.Log
import com.example.localassistant.llamacpp.Llama
import com.localassistant.engine.InferenceEngine
import com.localassistant.model.Message
import com.localassistant.model.TextMessage
import kotlinx.coroutines.flow.Flow

class LlamaCppInferenceEngine(
    private val modelPath: String
) : InferenceEngine {

    init {
        Log.d("LlamaCppInferenceEngine", "LlamaCppInferenceEngine initialized with model path: $modelPath")
    }

    override suspend fun load() {
        // The new JNI bridge loads the model on each generation, so this is a no-op.
    }

    override suspend fun generateResponse(prompt: String): Flow<String> {
        Log.d("LlamaCppInferenceEngine", "Formatted Prompt: $prompt")
        return Llama.generate(prompt, modelPath, 256)
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
        // The new JNI bridge releases resources after each generation, so this is a no-op.
    }
}