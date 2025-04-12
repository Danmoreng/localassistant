package com.example.localassistant.inference

class Phi4LlamaCppInference {

    private external fun loadModel(modelPath: String): Long

    fun loadModelFromFile(modelPath: String): Long {
        return loadModel(modelPath)
    }

    private external fun runInference(modelContext: Long, prompt: String): String
    fun runInferenceWithPrompt(modelContext: Long, prompt: String): String {
        return runInference(modelContext, prompt)
    }

    private external fun unloadModel(modelContext: Long)

    companion object {
        init {
            System.loadLibrary("llamacpp_jni")
        }
    }
}