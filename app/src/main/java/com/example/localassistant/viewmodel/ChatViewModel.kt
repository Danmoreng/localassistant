package com.example.localassistant.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localassistant.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // Backing list to store messages. Using a state list so that Compose
    // automatically re-renders when the list changes.
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    init {
        // Optionally, add a welcome message from the system or assistant
        _messages.add(
            TextMessage(
                text = "Welcome! How can I help you today?",
                type = MessageType.ASSISTANT
            )
        )
    }

    /**
     * Called when the user sends a message (e.g., from the ChatInput).
     */
    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val userMessage = TextMessage(
            text = text,
            type = MessageType.USER
        )
        _messages.add(0, userMessage)

        // Optionally generate an AI assistant response
        generateAssistantResponse(text)
    }

    /**
     * Generic method to send any type of Message (text, image, audio).
     * Use this if your UI can send different message types.
     */
    fun sendMessage(message: Message) {
        _messages.add(0, message)

        // If it's a user text message, respond
        if (message is TextMessage && message.type == MessageType.USER) {
            generateAssistantResponse(message.text)
        }
    }

    /**
     * Simulates generating an AI or system response after a short delay.
     * In a real app, you would call your local AI model here.
     */
    private fun generateAssistantResponse(userText: String) {
        viewModelScope.launch {
            // Simulate thinking
            delay(500L)

            // Example logic: echo user’s message or craft a custom response
            val responseText = "I received your message: \"$userText\""
            val assistantMessage = TextMessage(
                text = responseText,
                type = MessageType.ASSISTANT
            )
            _messages.add(0, assistantMessage)
        }
    }
}
