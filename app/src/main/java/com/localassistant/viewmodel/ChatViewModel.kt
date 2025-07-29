package com.localassistant.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localassistant.data.LlamaModelRepository
import com.localassistant.data.ModelDownloader
import com.localassistant.data.ModelRepository
import com.localassistant.data.Phi4ModelRepository
import com.localassistant.data.SettingsRepository
import com.localassistant.engine.InferenceEngine
import com.localassistant.inference.LlamaCppInferenceEngine
import com.localassistant.inference.OnnxInferenceEngine
import com.localassistant.model.Message
import com.localassistant.model.MessageType
import com.localassistant.model.TextMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val app: Application,
    private val settingsRepository: SettingsRepository,
    private val inferenceEngine: InferenceEngine,
    private val llamaModelRepository: LlamaModelRepository
) : ViewModel() {

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    private val _isModelAvailable = mutableStateOf(false)
    val isModelAvailable get() = _isModelAvailable

    private val _isDownloading = mutableStateOf(false)
    val isDownloading get() = _isDownloading

    private val _downloadError = mutableStateOf<String?>(null)
    val downloadError get() = _downloadError

    val systemPrompt = mutableStateOf("You are a helpful AI assistant.")

    init {
        viewModelScope.launch {
            inferenceEngine.load()
            _isModelAvailable.value = true
        }
        _messages.add(
            TextMessage(
                initialText = "Welcome! How can I help you today?",
                type = MessageType.ASSISTANT
            )
        )
    }

    fun sendMessage(message: Message) {
        _messages.add(0, message)

        if (message is TextMessage && message.type == MessageType.USER) {
            generateAssistantResponse()
        }
    }

    private fun generateAssistantResponse() {
        viewModelScope.launch {
            val prompt = inferenceEngine.formatChat(_messages, systemPrompt.value)
            val assistantMessage = TextMessage("", MessageType.ASSISTANT)
            _messages.add(0, assistantMessage)
            var accumulatedText = ""

            withContext(Dispatchers.IO) {
                inferenceEngine.generateResponse(prompt).collect { newToken ->
                    accumulatedText += newToken
                    viewModelScope.launch(Dispatchers.Main) {
                        assistantMessage.text = accumulatedText
                    }
                }
            }
        }
    }

    fun resetChat() {
        _messages.clear()
    }

    
}

