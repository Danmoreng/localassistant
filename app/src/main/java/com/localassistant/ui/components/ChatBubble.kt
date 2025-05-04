package com.localassistant.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.localassistant.model.*
import com.localassistant.model.AudioMessage
import com.localassistant.model.ImageMessage
import com.localassistant.model.Message
import com.localassistant.model.MessageType
import com.localassistant.model.TextMessage

@Composable
fun ChatBubble(message: Message) {
    // Choose colors based on message type
    val backgroundColor = when (message.type) {
        MessageType.USER -> MaterialTheme.colorScheme.primary
        MessageType.ASSISTANT -> MaterialTheme.colorScheme.surfaceVariant
        MessageType.SYSTEM -> MaterialTheme.colorScheme.surface
    }
    val textColor = when (message.type) {
        MessageType.USER -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        when (message) {
            is TextMessage -> {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(8.dp),
                    color = textColor
                )
            }

            is ImageMessage -> {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }

            is AudioMessage -> {
                AudioMessagePlayer(audioUrl = message.audioUrl)
            }
        }
    }
}

@Composable
fun AudioMessagePlayer(audioUrl: String) {
    // TODO: Replace with your actual audio player UI
    Text(
        text = "Audio message: $audioUrl",
        modifier = Modifier.padding(8.dp)
    )
}
