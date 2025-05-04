package com.localassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localassistant.ui.screens.ChatScreen
import com.localassistant.ui.screens.DownloadScreen
import com.localassistant.ui.theme.LocalAssistantTheme
import com.localassistant.viewmodel.ChatViewModel
import com.localassistant.viewmodel.DownloadViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalAssistantTheme {
                val downloadViewModel: DownloadViewModel = viewModel()
                val chatViewModel: ChatViewModel = viewModel()

                // Check if the model is available
                if (!downloadViewModel.isModelAvailable) {
                    // Show the download screen until the model is available.
                    DownloadScreen(viewModel = downloadViewModel)
                } else {
                    // When the model is available, display the chat screen
                    ChatScreen(
                        systemPrompt = chatViewModel.systemPrompt.value,
                        onSystemPromptChanged = { newPrompt ->
                            chatViewModel.systemPrompt.value = newPrompt
                        },
                        messages = chatViewModel.messages,
                        onMessageSent = { message ->
                            chatViewModel.sendMessage(message)
                        },
                        onResetChat = {
                            chatViewModel.resetChat()
                        }
                    )
                }
            }
        }
    }
}