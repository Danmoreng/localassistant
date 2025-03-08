package com.example.localassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localassistant.viewmodel.DownloadViewModel

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Model Download",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This downloads all ONNX files from the Hugging Face subfolder."
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isDownloading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.totalFiles > 0) {
                Text("Downloading file ${uiState.currentFileIndex} of ${uiState.totalFiles}")
                Text("Current: ${uiState.currentFileName}")
            } else {
                Text("Gathering file list...")
            }

        } else {
            uiState.errorMessage?.let { errorMsg ->
                Text(
                    text = "Error: $errorMsg",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(onClick = { viewModel.downloadModel() }) {
                Text("Download Model")
            }
        }
    }
}
