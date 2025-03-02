package com.example.localassistant.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localassistant.model.Message
import com.example.localassistant.model.MessageType

@Composable
fun ChatMessageRow(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = when (message.type) {
            MessageType.USER -> Arrangement.End
            else -> Arrangement.Start
        }
    ) {
        ChatBubble(message = message)
    }
}
