package com.localassistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localassistant.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEngineSelected: () -> Unit
) {
    val selectedEngine by viewModel.selectedEngine.collectAsState()
    val engines = listOf("phi", "llama")

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Inference Engine", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        engines.forEach { engine ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (engine == selectedEngine),
                        onClick = {
                            viewModel.setSelectedEngine(engine)
                            onEngineSelected()
                        }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (engine == selectedEngine),
                    onClick = {
                        viewModel.setSelectedEngine(engine)
                        onEngineSelected()
                    }
                )
                Text(
                    text = engine,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
