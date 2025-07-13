package com.localassistant.inference

import android.content.Context
import android.util.Log
import android.llama.cpp.LLamaAndroid
import com.localassistant.engine.InferenceEngine
import com.localassistant.model.AudioMessage
import com.localassistant.model.ImageMessage
import com.localassistant.model.Message
import com.localassistant.model.MessageType
import com.localassistant.model.TextMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class LlamaCppInferenceEngine(
    private val context: Context,
    private val modelPath: String
) : InferenceEngine {

    // Chat template tokens - these should ideally be loaded from the model's tokenizer config
    // For now, we'll use the same defaults as OnnxInferenceEngine
    private var userToken: String = "<|user|>"
    private var assistantToken: String = "<|assistant|>"
    private var systemToken: String = "<|system|>"
    private var endToken: String = "<end_of_turn>"
    private var bosToken: String = "" // Beginning of sequence
    private var eosToken: String = "" // End of sequence

    init {
        // Load the model when the engine is initialized
        // This should ideally be done in a coroutine or background thread
        // For simplicity, we'll assume it's handled by the LLamaAndroid singleton's load method
        // which is suspend fun and should be called from a coroutine scope.
        // The actual loading will happen when `LLamaAndroid.instance().load(modelPath)` is called.
        Log.d("LlamaCppInferenceEngine", "LlamaCppInferenceEngine initialized with model path: $modelPath")
    }

    override fun generateResponse(prompt: String): Flow<String> {
        Log.d("LlamaCppInferenceEngine", "Formatted Prompt: $prompt")
        // LLamaAndroid's send function expects the prompt to be already formatted
        return LLamaAndroid.instance().send(prompt, false).filter { it != endToken }
    }

    override fun formatChat(messages: List<Message>, systemPrompt: String): String {
        val sb = StringBuilder()

        // Add BOS token if available
        if (bosToken.isNotBlank()) {
            sb.append(bosToken)
        }

        // Insert system prompt if available.
        if (systemPrompt.isNotBlank()) {
            sb.append(systemToken).append("\n")
            sb.append(systemPrompt)
            sb.append(endToken).append("\n")
        }

        // The conversation list is stored with the newest message at index 0. Reverse it to start with the oldest.
        messages.reversed().forEach { message ->
            when (message) {
                is TextMessage -> {
                    when (message.type) {
                        MessageType.USER -> {
                            sb.append(userToken).append("\n")
                            sb.append(message.text)
                            sb.append(endToken).append("\n")
                        }
                        MessageType.ASSISTANT -> {
                            sb.append(assistantToken).append("\n")
                            sb.append(message.text)
                            sb.append(endToken).append("\n")
                        }
                        MessageType.SYSTEM -> {
                            // System messages within the conversation are handled by the initial systemPrompt
                            // If a system message appears mid-conversation, it might need special handling
                            // For now, we'll just append it as a regular system message.
                            sb.append(systemToken).append("\n")
                            sb.append(message.text)
                            sb.append(endToken).append("\n")
                        }
                    }
                }
                // TODO: Handle AudioMessage and ImageMessage if needed for chat formatting
                is AudioMessage -> TODO()
                is ImageMessage -> TODO()
            }
        }
        // Append the assistant token to prompt a new reply.
        sb.append(assistantToken)

        // Add EOS token if available
        if (eosToken.isNotBlank()) {
            sb.append(eosToken)
        }

        return sb.toString()
    }
}