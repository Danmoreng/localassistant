package com.example.localassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localassistant.ui.screens.ChatScreen
import com.example.localassistant.ui.screens.DownloadScreen
import com.example.localassistant.ui.theme.LocalAssistantTheme
import com.example.localassistant.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalAssistantTheme {
                // Obtain the ChatViewModel (which now also manages model download state)
                val chatViewModel: ChatViewModel = viewModel()
                val isModelAvailable by chatViewModel.isModelAvailable

                if (!isModelAvailable) {
                    // Show the download screen until the model is available
                    DownloadScreen(
                        isDownloading = chatViewModel.isDownloading.value,
                        downloadError = chatViewModel.downloadError.value,
                        onDownloadClicked = { chatViewModel.downloadModel() }
                    )
                } else {
                    // When the model is available, display the chat screen
                    ChatScreen(
                        messages = chatViewModel.messages,
                        onMessageSent = { message ->
                            chatViewModel.sendMessage(message)
                        }
                    )
                }
            }
        }
    }
}