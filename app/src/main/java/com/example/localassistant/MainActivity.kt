package com.example.localassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.example.localassistant.ui.screens.ChatScreen
import com.example.localassistant.ui.theme.LocalAssistantTheme
import com.example.localassistant.viewmodel.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalAssistantTheme {
                // Obtain the ViewModel
                val chatViewModel: ChatViewModel = viewModel()

                // Pass state and event callbacks to ChatScreen
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