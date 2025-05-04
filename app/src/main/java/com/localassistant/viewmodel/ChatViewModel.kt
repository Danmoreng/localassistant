package com.localassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localassistant.data.ModelDownloader
import com.localassistant.data.Phi4ModelRepository
import com.localassistant.inference.Phi4OnnxInference
import com.localassistant.model.AudioMessage
import com.localassistant.model.ImageMessage
import com.localassistant.model.Message
import com.localassistant.model.MessageType
import com.localassistant.model.TextMessage
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
    private val repository = Phi4ModelRepository(application, ModelDownloader())
    private var onnxInference: Phi4OnnxInference? = null

    init {
        // Optionally, add a welcome message (if desired, you may later remove this during reset)
        _messages.add(
            TextMessage(
                initialText = "Welcome! How can I help you today?",
                type = MessageType.ASSISTANT
            )
        )

        viewModelScope.launch {
            val available = repository.isModelAvailable() // <-- Suspend call
            _isModelAvailable.value = available

            if (available) {
                initOnnxInference()
            }
        }
    }

    private fun initOnnxInference() {
        try {
            // Get the absolute path where the model files were stored
            val modelDirPath = repository.getModelDirectory()
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


    // -----------------------------
    // 5) Chat logic
    // -----------------------------

    fun sendMessage(message: Message) {
        _messages.add(0, message)

        if (message is TextMessage && message.type == MessageType.USER) {
            generateAssistantResponse()
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

    private fun generateAssistantResponse() {
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
