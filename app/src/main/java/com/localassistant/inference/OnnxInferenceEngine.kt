package com.localassistant.inference

import android.content.Context
import android.util.Log
import ai.onnxruntime.genai.GenAIException
import ai.onnxruntime.genai.Generator
import ai.onnxruntime.genai.GeneratorParams
import ai.onnxruntime.genai.Model
import ai.onnxruntime.genai.Tokenizer
import com.localassistant.engine.InferenceEngine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OnnxInferenceEngine(
    private val context: Context,
    private val modelDirPath: String
) : InferenceEngine {
    private val model: Model
    private val tokenizer: Tokenizer

    init {
        try {
            // 1) Load the model from the given folder
            model = Model(modelDirPath)
            Log.d("OnnxInferenceEngine", "Model loaded from $modelDirPath")

            // 2) Create the tokenizer from the model
            tokenizer = Tokenizer(model)
            Log.d("OnnxInferenceEngine", "Tokenizer created successfully.")

        } catch (ex: GenAIException) {
            throw RuntimeException("Failed to initialize GenAI Model/Tokenizer", ex)
        }
    }

    override fun generateResponse(prompt: String): Flow<String> = callbackFlow {
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
}