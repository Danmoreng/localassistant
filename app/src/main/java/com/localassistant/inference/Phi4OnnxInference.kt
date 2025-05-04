package com.localassistant.inference

import android.content.Context
import android.util.Log
import ai.onnxruntime.genai.GenAIException
import ai.onnxruntime.genai.Model
import ai.onnxruntime.genai.Tokenizer
import ai.onnxruntime.genai.GeneratorParams
import ai.onnxruntime.genai.Generator

class Phi4OnnxInference(
    private val context: Context,
    private val modelDirPath: String
) {
    private val model: Model
    private val tokenizer: Tokenizer

    init {
        try {
            // 1) Load the model from the given folder
            model = Model(modelDirPath)
            Log.d("Phi4OnnxInference", "Model loaded from $modelDirPath")

            // 2) Create the tokenizer from the model
            tokenizer = Tokenizer(model)
            Log.d("Phi4OnnxInference", "Tokenizer created successfully.")

        } catch (ex: GenAIException) {
            throw RuntimeException("Failed to initialize GenAI Model/Tokenizer", ex)
        }
    }

    /**
     * Streams generated tokens by calling the [onToken] callback with each new token.
     *
     * @param prompt Prompt in chat template format
     * @param onToken A callback invoked on each new token (or group of tokens) as they’re generated.
     */
    fun streamText(prompt: String, onToken: (String) -> Unit) {
        try {
            // Encode the prompt to tokens.
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
                    onToken(tokenText)
                }
            }
        } catch (ex: GenAIException) {
            onToken("Error during generation: ${ex.message}")
        }
    }
}
