package com.example.localassistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localassistant.model.Message
import com.example.localassistant.model.MessageType
import com.example.localassistant.model.TextMessage
import com.example.localassistant.ui.components.ChatInput
import com.example.localassistant.ui.components.ChatMessageRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    systemPrompt: String,
    onSystemPromptChanged: (String) -> Unit,
    messages: List<Message>,
    onMessageSent: (Message) -> Unit,
    onResetChat: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                actions = {
                    Button(onClick = onResetChat) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { innerPadding ->
        // The innerPadding provided here automatically accounts for the top and bottom bars.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)  // Optional additional padding
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageRow(message = message)
                }
                item {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = onSystemPromptChanged,
                        label = { Text("System Prompt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            ChatInput { inputText ->
                onMessageSent(
                    TextMessage(
                        inputText,
                        type = MessageType.USER
                    )
                )
            }
        }
    }
}
