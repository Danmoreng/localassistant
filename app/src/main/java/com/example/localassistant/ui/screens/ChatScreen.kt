package com.example.localassistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localassistant.model.Message
import com.example.localassistant.model.MessageType
import com.example.localassistant.model.TextMessage
import com.example.localassistant.ui.components.ChatInput
import com.example.localassistant.ui.components.ChatMessageRow

@Composable
fun ChatScreen(
    messages: List<Message>,
    onMessageSent: (Message) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageRow(message = message)
            }
        }
        ChatInput { inputText ->
            onMessageSent(TextMessage(
                inputText,
                type = MessageType.USER
            ))
        }
    }
}
