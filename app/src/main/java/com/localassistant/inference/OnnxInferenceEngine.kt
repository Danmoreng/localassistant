package com.localassistant.inference

import android.content.Context
import android.util.Log
import ai.onnxruntime.genai.GenAIException
import ai.onnxruntime.genai.Generator
import ai.onnxruntime.genai.GeneratorParams
import ai.onnxruntime.genai.Model
import ai.onnxruntime.genai.Tokenizer
import com.localassistant.engine.InferenceEngine
import com.localassistant.model.AudioMessage
import com.localassistant.model.ImageMessage
import com.localassistant.model.Message
import com.localassistant.model.MessageType
import com.localassistant.model.TextMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import org.json.JSONObject

class OnnxInferenceEngine(
    private val context: Context,
    private val modelDirPath: String
) : InferenceEngine {
    private val model: Model
    private val tokenizer: Tokenizer

    // Chat template tokens
    private var userToken: String = "<|user|>"
    private var assistantToken: String = "<|assistant|>"
    private var systemToken: String = "<|system|>"
    private var endToken: String = "<|end|>"
    private var bosToken: String = "" // Beginning of sequence
    private var eosToken: String = "" // End of sequence

    init {
        try {
            // 1) Load the model from the given folder
            model = Model(modelDirPath)
            Log.d("OnnxInferenceEngine", "Model loaded from $modelDirPath")

            // 2) Create the tokenizer from the model
            tokenizer = Tokenizer(model)
            Log.d("OnnxInferenceEngine", "Tokenizer created successfully.")

            // Load chat template tokens from tokenizer_config.json and special_tokens_map.json
            loadChatTemplateTokens()

        } catch (ex: GenAIException) {
            throw RuntimeException("Failed to initialize GenAI Model/Tokenizer", ex)
        }
    }

    private fun loadChatTemplateTokens() {
        try {
            val tokenizerConfigFile = File(modelDirPath, "tokenizer_config.json")
            if (tokenizerConfigFile.exists()) {
                val configJson = JSONObject(tokenizerConfigFile.readText())
                bosToken = configJson.optString("bos_token", "")
                if (bosToken == "<|endoftext|>") { // Add this check
                    bosToken = ""
                }
                eosToken = configJson.optString("eos_token", "")
            }

            val specialTokensMapFile = File(modelDirPath, "special_tokens_map.json")
            if (specialTokensMapFile.exists()) {
                val specialTokensJson = JSONObject(specialTokensMapFile.readText())
                // This is a simplified approach. Real parsing would involve iterating through arrays or specific keys.
                // For Phi-4-mini, the tokens are often just "<|user|>", "<|assistant|>", "<|system|>", "<|end|>"
                // which are already set as defaults.
                // We can try to extract them if they are explicitly defined in the special_tokens_map.json
                // For example, if "additional_special_tokens" is an array of objects with "content" field.
                // For now, we rely on the defaults and the model's understanding.
            }
            Log.d("OnnxInferenceEngine", "Loaded chat template tokens: user=$userToken, assistant=$assistantToken, system=$systemToken, end=$endToken, bos=$bosToken, eos=$eosToken")

        } catch (e: Exception) {
            Log.e("OnnxInferenceEngine", "Error loading chat template tokens: ${e.message}")
            // Fallback to default tokens
        }
    }

    override suspend fun load() {
        // ONNX model is loaded in the init block, so this is empty
    }

    override fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        try {
            // Encode the prompt to tokens.
            Log.d("OnnxInferenceEngine", "Formatted Prompt: $prompt")
            val inputSequences = tokenizer.encode(prompt)
            val inputTokens = inputSequences.getSequence(0)

            // Set up the generator parameters (using max_length similar to your Python example).
            val generatorParams = GeneratorParams(model)
            generatorParams.setSearchOption("max_length", 2048.0)

            // Create a Generator instance.
            Generator(model, generatorParams).use { generator ->
                // Append the input tokens.
                generator.appendTokens(inputTokens)
                // Use the Generator's iterator to stream tokens.
                for (token in generator) {
                    // Decode the individual token (wrap it in an array).
                    val tokenText = tokenizer.decode(intArrayOf(token))
                    // Emit the token text through the callback.
                    send(tokenText)
                }
            }
        } catch (ex: GenAIException) {
            send("Error during generation: ${ex.message}")
        }
        awaitClose {
            // Close any resources if necessary
        }
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
            sb.append(endToken)
        }

        // The conversation list is stored with the newest message at index 0. Reverse it to start with the oldest.
        messages.reversed().forEach { message ->
            when (message) {
                is TextMessage -> {
                    when (message.type) {
                        MessageType.USER -> {
                            sb.append(userToken).append("\n")
                            sb.append(message.text)
                            sb.append(endToken)
                        }
                        MessageType.ASSISTANT -> {
                            sb.append(assistantToken).append("\n")
                            sb.append(message.text)
                            sb.append(endToken)
                        }
                        MessageType.SYSTEM -> {
                            // System messages within the conversation are handled by the initial systemPrompt
                            // If a system message appears mid-conversation, it might need special handling
                            // For now, we'll just append it as a regular system message.
                            sb.append(systemToken).append("\n")
                            sb.append(message.text)
                            sb.append(endToken)
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

    override suspend fun close() {
        // No-op
    }
}
