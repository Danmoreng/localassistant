package com.example.localassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localassistant.data.Phi4ModelRepository
import com.example.localassistant.inference.Phi4OnnxInference
import com.example.localassistant.model.Message
import com.example.localassistant.model.MessageType
import com.example.localassistant.model.TextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {

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
    // 3) Model repository
    // -----------------------------
    private val modelRepository = Phi4ModelRepository(application)
    private var onnxInference: Phi4OnnxInference? = null

    init {
        // Add a welcome message
        _messages.add(
            TextMessage(
                text = "Welcome! How can I help you today?",
                type = MessageType.ASSISTANT
            )
        )

        // Check if model files already exist
        _isModelAvailable.value = modelRepository.isModelAvailable()

        // If model files are already there, initialize the inference session
        if (_isModelAvailable.value) {
            initOnnxInference()
        }
    }

    private fun initOnnxInference() {
        try {
            // Get the absolute path where the model files were stored
            val modelDirPath = modelRepository.getModelDirPath()
            // or modelRepository.modelDirectory.absolutePath, depending on your code

            onnxInference = Phi4OnnxInference(
                context = getApplication(), // or requireContext() if inside Fragment
                modelDirPath = modelDirPath
            )

            Log.d("ChatViewModel", "ONNX Inference initialized.")

        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to init Onnx Inference", e)
        }
    }


    /**
     * Called when the user wants to initiate a download of the model files.
     */
fun downloadModel() {
    Log.d("ChatViewModel", "Download model triggered")
    viewModelScope.launch(Dispatchers.IO) { // Use Dispatchers.IO here
        _isDownloading.value = true
        _downloadError.value = null

            try {
                modelRepository.downloadModelFiles()
                _isModelAvailable.value = true
                // Once downloaded, initialize the inference
                initOnnxInference()
            } catch (e: Exception) {
                _downloadError.value = e.message
                Log.e("ChatViewModel", "Download failed", e)
            } finally {
                _isDownloading.value = false
            }
        }
    }


    // -----------------------------
    // 4) Chat logic
    // -----------------------------
    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val userMessage = TextMessage(text, MessageType.USER)
        _messages.add(0, userMessage)

        // Optionally generate an AI assistant response
        generateAssistantResponse(text)
    }

    fun sendMessage(message: Message) {
        _messages.add(0, message)

        if (message is TextMessage && message.type == MessageType.USER) {
            generateAssistantResponse(message.text)
        }
    }

    private fun generateAssistantResponse(userText: String) {
        viewModelScope.launch {
            // Move heavy inference off main thread
            val responseText = withContext(Dispatchers.IO) {
                onnxInference?.generateText(userText)
                    ?: "Model not initialized yet."
            }
            val assistantMessage = TextMessage(responseText, MessageType.ASSISTANT)
            _messages.add(0, assistantMessage)
        }
    }
}
