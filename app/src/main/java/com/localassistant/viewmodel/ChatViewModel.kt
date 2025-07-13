package com.localassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localassistant.data.ModelRepository
import com.localassistant.engine.InferenceEngine
import com.localassistant.model.AudioMessage
import com.localassistant.model.ImageMessage
import com.localassistant.model.Message
import com.localassistant.model.MessageType
import com.localassistant.model.TextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    application: Application,
    private val repository: ModelRepository,
    private val inferenceEngine: InferenceEngine
) : AndroidViewModel(application) {

    // -----------------------------
    // 1) Chat messages state
    // -----------------------------
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    // -----------------------------
    // 2) Model availability states
    // -----------------------------
    private val _isModelAvailable = mutableStateOf(false)
    val isModelAvailable get() = _isModelAvailable

    private val _isDownloading = mutableStateOf(false)
    val isDownloading get() = _isDownloading

    private val _downloadError = mutableStateOf<String?>(null)
    val downloadError get() = _downloadError

    // -----------------------------
    // 3) System prompt state
    // -----------------------------
    val systemPrompt = mutableStateOf("You are a helpful AI assistant.")

    init {
        // Optionally, add a welcome message (if desired, you may later remove this during reset)
        _messages.add(
            TextMessage(
                initialText = "Welcome! How can I help you today?",
                type = MessageType.ASSISTANT
            )
        )
        viewModelScope.launch {
            _isModelAvailable.value = repository.isModelAvailable()
        }
    }

    // -----------------------------
    // 5) Chat logic
    // -----------------------------

    fun sendMessage(message: Message) {
        _messages.add(0, message)

        if (message is TextMessage && message.type == MessageType.USER) {
            generateAssistantResponse()
        }
    }

    

    

    private fun generateAssistantResponse() {
        viewModelScope.launch {
            // Build the prompt using the current conversation history and system prompt.
            val prompt = inferenceEngine.formatChat(_messages, systemPrompt.value)
            // Create an empty assistant message for streaming response.
            val assistantMessage = TextMessage("", MessageType.ASSISTANT)
            _messages.add(0, assistantMessage)
            // Buffer for accumulating generated tokens.
            var accumulatedText = ""

            // Run generation on IO thread.
            withContext(Dispatchers.IO) {
                inferenceEngine.generateResponse(prompt).collect { newToken ->
                    accumulatedText += newToken
                    // Update UI on the main thread.
                    viewModelScope.launch(Dispatchers.Main) {
                        assistantMessage.text = accumulatedText
                    }
                }
            }
        }
    }

    /**
     * Reset the chat by clearing all messages.
     * The system prompt (stored separately) is preserved.
     */
    fun resetChat() {
        _messages.clear()
    }

    fun resetInferenceEngine() {
        // This function will be used to trigger the recomposition of the ChatScreen
    }
}
