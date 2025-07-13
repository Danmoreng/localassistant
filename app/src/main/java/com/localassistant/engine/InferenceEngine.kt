package com.localassistant.engine

import com.localassistant.model.Message
import kotlinx.coroutines.flow.Flow

interface InferenceEngine {
    fun generateResponse(prompt: String): Flow<String>
    fun formatChat(messages: List<Message>, systemPrompt: String): String
}
