package com.example.localassistant.model

enum class MessageType {
    SYSTEM,
    ASSISTANT,
    USER
}

sealed class Message {
    abstract val type: MessageType
}

data class TextMessage(
    val text: String,
    override val type: MessageType
) : Message()

data class ImageMessage(
    val imageUrl: String,
    override val type: MessageType
) : Message()

data class AudioMessage(
    val audioUrl: String,
    override val type: MessageType
) : Message()
