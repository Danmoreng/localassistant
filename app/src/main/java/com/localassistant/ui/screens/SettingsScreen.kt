package com.localassistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localassistant.engine.Engine

@Composable
fun SettingsScreen(
    selectedEngine: Engine,
    onEngineSelected: (Engine) -> Unit
) {
    val engines = Engine.values()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Inference Engine", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        engines.forEach { engine ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (engine == selectedEngine),
                        onClick = { onEngineSelected(engine) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (engine == selectedEngine),
                    onClick = { onEngineSelected(engine) }
                )
                Text(
                    text = engine.name,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
