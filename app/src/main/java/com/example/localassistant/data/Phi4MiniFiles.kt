package com.example.localassistant.data

object Phi4MiniFiles {
    // Subfolder in the HF repo
    private const val HF_SUBFOLDER = "cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4"
    // Base Hugging Face URL (use 'resolve' so HF handles versioning)
    private const val HF_BASE_URL = "https://huggingface.co/microsoft/Phi-4-mini-instruct-onnx/resolve/main/$HF_SUBFOLDER"

    // List all files you need
    val REQUIRED_FILES = listOf(
        "config.json",
        "genai_config.json",
        "merges.txt",
        "model.onnx",
        "model.onnx.data",
        "special_tokens_map.json",
        "tokenizer.json",
        "tokenizer_config.json",
        "vocab.json"
    )

    /**
     * Build the direct download URL for a given file.
     */
    fun getDownloadUrl(fileName: String): String {
        return "$HF_BASE_URL/$fileName"
    }
}
