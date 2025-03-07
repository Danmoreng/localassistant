package com.example.localassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localassistant.data.Phi4ModelRepository
import com.example.localassistant.inference.Phi4OnnxInference
import com.example.localassistant.model.AudioMessage
import com.example.localassistant.model.ImageMessage
import com.example.localassistant.model.Message
import com.example.localassistant.model.MessageType
import com.example.localassistant.model.TextMessage
import kotlinx.coroutines.Dispatchers
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
    // 3) System prompt state
    // -----------------------------
    val systemPrompt = mutableStateOf("You are a helpful AI assistant.")

    // -----------------------------
    // 4) Model repository
    // -----------------------------
    private val modelRepository = Phi4ModelRepository(application)
    private var onnxInference: Phi4OnnxInference? = null

    init {
        // Optionally, add a welcome message (if desired, you may later remove this during reset)
        _messages.add(
            TextMessage(
                initialText = "Welcome! How can I help you today?",
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
            val modelDirPath = modelRepository.getModelDirectory()
            // or modelRepository.modelDirectory.absolutePath, depending on your code

            onnxInference = Phi4OnnxInference(
                context = getApplication(),
                modelDirPath = modelDirPath.toString()
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
    // 5) Chat logic
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

    /**
     * Build the prompt for the model.
     * We insert the system prompt at the top and then append the rest of the conversation.
     */
    private fun buildPrompt(conversation: List<Message>): String {
        // The conversation list is stored with the newest message at index 0.
        // Reverse it to start with the oldest message.
        val sb = StringBuilder()
        // Insert system prompt if available.
        if (systemPrompt.value.isNotBlank()) {
            sb.append("<|system|>\n")
            sb.append(systemPrompt.value)
            sb.append(" <|end|>\n")
        }
        // The conversation list is stored with the newest message at index 0. Reverse it to start with the oldest.
        conversation.reversed().forEach { message ->
            when (message) {
                is TextMessage -> {
                    when (message.type) {
                        MessageType.USER -> {
                            // Append the user message using the chat template.
                            sb.append("<|user|>\n")
                            sb.append(message.text)
                            sb.append(" <|end|>\n")
                        }
                        MessageType.ASSISTANT -> {
                            sb.append("<|assistant|>\n")
                            sb.append(message.text)
                            sb.append(" <|end|>\n")
                        }
                        // Add other message types if needed.
                        MessageType.SYSTEM -> TODO()
                    }
                }
                is AudioMessage -> { /* TODO: Handle audio if needed */ }
                is ImageMessage -> { /* TODO: Handle image if needed */ }
            }
        }
        // Append the assistant token to prompt a new reply.
        sb.append("<|assistant|>")
        return sb.toString()
    }

    private fun cleanResponse(response: String): String {
        return response
            .replace("<|assistant|>", "")
            .replace("<|user|>", "")
            .replace("<|end|>", "")
            .trim()
    }

    private fun extractAssistantReply(fullText: String): String {
        val marker = "<|assistant|>"
        val index = fullText.lastIndexOf(marker)
        val reply = if (index != -1) {
            fullText.substring(index + marker.length)
        } else {
            fullText
        }
        return cleanResponse(reply)
    }

    private fun generateAssistantResponse(userText: String) {
        viewModelScope.launch {
            // Build the prompt using the current conversation history (without the new assistant message).
            val prompt = buildPrompt(_messages)
            // Create an empty assistant message for streaming response.
            val assistantMessage = TextMessage("", MessageType.ASSISTANT)
            _messages.add(0, assistantMessage)
            // Buffer for accumulating generated tokens.
            var accumulatedText = ""

            // Run generation on IO thread.
            withContext(Dispatchers.IO) {
                onnxInference?.streamText(prompt) { newToken ->
                    accumulatedText += newToken
                    // Extract the actual assistant reply.
                    val cleanedReply = extractAssistantReply(accumulatedText)
                    // Update UI on the main thread.
                    viewModelScope.launch(Dispatchers.Main) {
                        assistantMessage.text = cleanedReply
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
}
