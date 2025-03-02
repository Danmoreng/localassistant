package com.example.localassistant.inference

import android.content.Context
import android.util.Log
import ai.onnxruntime.genai.GenAIException
import ai.onnxruntime.genai.Model
import ai.onnxruntime.genai.Tokenizer
import ai.onnxruntime.genai.Sequences
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
     * Runs a single-shot generation:
     * - Encode prompt -> token IDs
     * - Create GeneratorParams, set any generation options
     * - Instantiate a Generator, feed in tokens, call generate()
     * - Decode final sequences
     */
    fun generateText(prompt: String): String {
        return try {
            // Encode prompt into IDs
            val inputTokens = tokenizer.encode(prompt)

            // Build generator parameters
            val generatorParams = GeneratorParams(model)
            // For example, set max_length
            generatorParams.setSearchOption("max_length", 512.0)
            // You can add more:
            // generatorParams.setSearchOption("temperature", 0.7)
            // generatorParams.setSearchOption("top_p", 0.9)
            // etc.

            // Create a new generator
            val generator = Generator(model, generatorParams)

            // Feed the prompt tokens
            generator.appendTokens(inputTokens)

            // Perform single-shot generation
            generator.generate()

            // Retrieve the generated sequences (e.g., first sequence if batch size = 1)
            val outputSequences = generator.getOutputSequences()
            if (outputSequences.isEmpty()) {
                "No output tokens were generated."
            } else {
                val firstSequence = outputSequences[0]
                // Decode that token array back to string
                val decodedText = tokenizer.decode(firstSequence)
                Log.d("Phi4OnnxInference", "Generated text: $decodedText")

                decodedText
            }

        } catch (ex: GenAIException) {
            Log.e("Phi4OnnxInference", "Generation failed: ${ex.message}", ex)
            "Error during generation: ${ex.message}"
        }
    }
}
