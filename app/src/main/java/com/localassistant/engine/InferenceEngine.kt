package com.localassistant.engine

import com.localassistant.model.Message
import kotlinx.coroutines.flow.Flow

interface InferenceEngine {
    suspend fun load()
    suspend fun generateResponse(prompt: String): Flow<String>
    fun formatChat(messages: List<Message>, systemPrompt: String): String
    suspend fun close()
}
