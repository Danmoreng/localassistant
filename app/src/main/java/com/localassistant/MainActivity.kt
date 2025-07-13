package com.localassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localassistant.data.LlamaModelRepository
import com.localassistant.data.ModelDownloader
import com.localassistant.data.Phi4ModelRepository
import com.localassistant.engine.Engine
import com.localassistant.inference.LlamaCppInferenceEngine
import com.localassistant.inference.OnnxInferenceEngine
import com.localassistant.ui.screens.ChatScreen
import com.localassistant.ui.screens.DownloadScreen
import com.localassistant.ui.screens.SettingsScreen
import com.localassistant.ui.theme.LocalAssistantTheme
import com.localassistant.viewmodel.ChatViewModel
import com.localassistant.viewmodel.DownloadViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalAssistantTheme {
                var selectedEngine by rememberSaveable { mutableStateOf(Engine.ONNX) }
                var showSettings by rememberSaveable { mutableStateOf(false) }

                val downloadViewModel: DownloadViewModel = viewModel(
                    key = selectedEngine.name, // Recreate ViewModel when engine changes
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            val repository = when (selectedEngine) {
                                Engine.LLAMA_CPP -> LlamaModelRepository(
                                    context = application,
                                    remoteDataSource = ModelDownloader()
                                )
                                Engine.ONNX -> Phi4ModelRepository(
                                    context = application,
                                    remoteDataSource = ModelDownloader()
                                )
                            }
                            return DownloadViewModel(
                                application = application,
                                repository = repository
                            ) as T
                        }
                    }
                )

                if (showSettings) {
                    SettingsScreen(
                        selectedEngine = selectedEngine,
                        onEngineSelected = { engine ->
                            selectedEngine = engine
                            showSettings = false
                        }
                    )
                } else if (!downloadViewModel.isModelAvailable) {
                    DownloadScreen(viewModel = downloadViewModel)
                } else {
                    val chatViewModel: ChatViewModel = viewModel(
                        key = selectedEngine.name, // Recreate ViewModel when engine changes
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                val repository = when (selectedEngine) {
                                    Engine.LLAMA_CPP -> LlamaModelRepository(
                                        context = application,
                                        remoteDataSource = ModelDownloader()
                                    )
                                    Engine.ONNX -> Phi4ModelRepository(
                                        context = application,
                                        remoteDataSource = ModelDownloader()
                                    )
                                }

                                val inferenceEngine = when (selectedEngine) {
                                    Engine.LLAMA_CPP -> LlamaCppInferenceEngine(
                                        modelPath = (repository as LlamaModelRepository).getModelPath()
                                    )
                                    Engine.ONNX -> OnnxInferenceEngine(
                                        context = application,
                                        modelDirPath = repository.getModelDirectory().absolutePath
                                    )
                                }

                                return ChatViewModel(
                                    application = application,
                                    repository = repository,
                                    inferenceEngine = inferenceEngine
                                ) as T
                            }
                        }
                    )
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
