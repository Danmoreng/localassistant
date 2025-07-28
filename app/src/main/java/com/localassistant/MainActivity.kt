package com.localassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.localassistant.ui.screens.ChatScreen
import com.localassistant.ui.screens.DownloadScreen
import com.localassistant.ui.screens.SettingsScreen
import com.localassistant.ui.theme.LocalAssistantTheme
import com.localassistant.viewmodel.ChatViewModel
import com.localassistant.viewmodel.DownloadViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalAssistantTheme {
                var showSettings by rememberSaveable { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(onEngineSelected = {
                        showSettings = false
                        downloadViewModel.resetRepository()
                    })
                } else {
                    var isModelAvailabilityChecked by rememberSaveable { mutableStateOf(false) }

                    LaunchedEffect(downloadViewModel) {
                        isModelAvailabilityChecked = false
                        downloadViewModel.checkModelAvailability()
                        isModelAvailabilityChecked = true
                    }

                    if (!isModelAvailabilityChecked) {
                        // You can show a loading indicator here
                    } else if (!downloadViewModel.isModelAvailable) {
                        DownloadScreen(viewModel = downloadViewModel)
                    } else {
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
                            },
                            onSettingsClicked = {
                                showSettings = true
                            }
                        )
                    }
                }
            }
        }
    }
}

