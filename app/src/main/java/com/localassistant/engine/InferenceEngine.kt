package com.localassistant.engine

import kotlinx.coroutines.flow.Flow

interface InferenceEngine {
    fun generateResponse(prompt: String): Flow<String>
}
