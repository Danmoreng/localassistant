# Gemini Code Understanding

This document outlines the understanding of the LocalAssistant Android project by the Gemini agent.

## Project Overview

This is an Android application that allows users to chat with a local Large Language Model (LLM). The app is built using Kotlin and Jetpack Compose, following an MVVM architecture.

## Core Functionality

1.  **Model Download:**
    *   The app first checks if the LLM model is available on the device.
    *   If the model is not available, it displays a download screen (`DownloadScreen`).
    *   The `DownloadViewModel` handles the model download process. It downloads the model from a hardcoded URL.
    *   The downloaded model is stored in the device's cache directory.

2.  **Chat Interface:**
    *   Once the model is available, the app displays a chat screen (`ChatScreen`).
    *   The `ChatViewModel` manages the chat state and interactions.
    *   Users can send messages to the LLM and receive responses.
    *   The chat history is displayed on the screen.
    *   Users can change the system prompt and reset the chat.

3.  **Inference:**
    *   The `InferenceRepository` (now abstracted) is responsible for interacting with the LLM.
    *   It loads the downloaded model and uses it to generate responses to user messages.
    *   It takes the user's message and the chat history as input and returns a stream of generated tokens as a response.

## Code Structure

*   **`LocalAssistantApp.kt`**: The Application class, annotated with `@HiltAndroidApp` to enable Hilt dependency injection.
*   **`MainActivity.kt`**: The main entry point of the app, annotated with `@AndroidEntryPoint`. It now uses Hilt to inject ViewModels and no longer contains manual ViewModel factories.
*   **`di` package**:
    *   `AppModule.kt`: A Hilt module that provides singleton instances of `ModelRepository` and `InferenceEngine`.
*   **`data` package**:
    *   `ModelRepository.kt`: New interface for abstracting model download and management.
    *   `Phi4ModelRepository.kt`: Implementation of `ModelRepository` for Phi-4 models.
    *   `DownloadProgress.kt`: Data class for download progress.
    *   `ModelDownloader.kt`: Handles actual model downloading.
    *   `Phi4MiniFiles.kt`: Specific file definitions for Phi-4 mini.
    *   `RemoteModelDataSource.kt`: Handles remote model data.
    *   `SettingsRepository.kt`: Interface for managing user settings.
*   **`engine` package**: (Moved from `core/engine`)
    *   `InferenceEngine.kt`: Modified interface for abstracting inference engines, now supporting streaming with `Flow`.
    *   `Engine.kt`: Placeholder class.
*   **`inference` package**:
    *   `OnnxInferenceEngine.kt`: New implementation of `InferenceEngine` for ONNX models (replaces `Phi4OnnxInference.kt`).
*   **`model` package**:
    *   `Message.kt`: A data class representing a chat message.
    *   `AudioMessage.kt`, `ImageMessage.kt`, `TextMessage.kt`: Specific message types.
    *   `MessageType.kt`: Enum for message types.
*   **`ui` package**:
    *   `ChatScreen.kt`: The main chat screen UI.
    *   `DownloadScreen.kt`: The model download screen UI.
*   **`viewmodel` package**:
    *   `ChatViewModel.kt`: The ViewModel for the `ChatScreen`, now annotated with `@HiltViewModel` and receiving dependencies via constructor injection.
    *   `DownloadViewModel.kt`: The ViewModel for the `DownloadScreen`, now annotated with `@HiltViewModel` and receiving dependencies via constructor injection.

## Key Observations

*   The app uses a hardcoded URL to download the model. This might not be ideal for a production app, but it's fine for a demo.
*   The app now uses abstracted interfaces (`InferenceEngine`, `ModelRepository`) for better modularity and to support multiple inference engines in the future.
*   The `InferenceEngine` now supports streaming responses using Kotlin `Flow`.
*   **Dependency injection is now handled by Hilt.** Manual factories in `MainActivity.kt` have been removed.
*   The app has a clean and simple architecture that is easy to understand and extend.
*   **Working Environment:** This agent is aware that it is operating in a PowerShell 7 environment on Windows. The user runs builds and tests from within Android Studio.
