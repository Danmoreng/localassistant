package com.example.localassistant.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class MessageType {
    SYSTEM,
    ASSISTANT,
    USER
}

sealed class Message {
    abstract val type: MessageType
}

class TextMessage(
    initialText: String,
    override val type: MessageType
) : Message() {
    var text by mutableStateOf(initialText)
}

data class ImageMessage(
    val imageUrl: String,
    override val type: MessageType
) : Message()

data class AudioMessage(
    val audioUrl: String,
    override val type: MessageType
) : Message()
