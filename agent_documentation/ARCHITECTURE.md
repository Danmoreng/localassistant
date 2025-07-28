# LocalAssistant Architecture

> **Last Updated:** 28 Jul 2025

This document provides a comprehensive overview of the LocalAssistant Android application's architecture.

## 1. Core Principles

The application is built upon modern Android development best practices, emphasizing a clean, scalable, and maintainable codebase.

- **Separation of Concerns:** The app follows the **Model-View-ViewModel (MVVM)** pattern, strictly separating the UI (View), state management (ViewModel), and data/business logic (Model).
- **Dependency Injection:** **Hilt** is used for managing dependencies, which simplifies the architecture and improves testability by decoupling components.
- **Reactivity:** The UI is built with **Jetpack Compose**, and state is exposed using **Kotlin Flows**, creating a reactive and declarative user interface.
- **Abstraction:** Key functionalities like model management and inference are hidden behind interfaces (`ModelRepository`, `InferenceEngine`), allowing for multiple implementations (e.g., ONNX, Llama.cpp) to be used interchangeably.

## 2. Component Breakdown

### View (`ui` package)
- **`MainActivity.kt`**: The single entry point of the app, annotated with `@AndroidEntryPoint`. It hosts the different screens.
- **`ChatScreen.kt` & `DownloadScreen.kt`**: Composable functions that display the UI. They are stateless and react to data exposed by their respective ViewModels.
- **`SettingsScreen.kt`**: A Composable for selecting the inference engine.

### ViewModel (`viewmodel` package)
- **`ChatViewModel.kt` & `DownloadViewModel.kt`**: Annotated with `@HiltViewModel`, these classes manage UI state, handle user events, and interact with the Model layer. They are injected with the necessary repositories and engines by Hilt.

### Model (Data & Business Logic)
This layer is composed of several key components across different packages:

- **Repositories (`data` package):**
    - **`ModelRepository`**: An interface defining the contract for model management (downloading, checking existence).
        - **`Phi4ModelRepository`**: Implementation for ONNX models.
        - **`LlamaModelRepository`**: Implementation for GGUF models.
    - **`SettingsRepository`**: Manages user preferences, such as the selected engine.

- **Inference Engines (`engine` & `inference` packages):**
    - **`InferenceEngine`**: An interface that defines the contract for the LLM inference process. It takes a chat history and returns a `Flow` of response tokens.
        - **`OnnxInferenceEngine`**: Implementation using the ONNX Runtime.
        - **`LlamaCppInferenceEngine`**: Implementation using the `llama.cpp` library via a JNI bridge.

- **Data Models (`model` package):**
    - Simple, immutable data classes (e.g., `Message`) that represent the application's data structures.

## 3. Dependency Injection with Hilt

Hilt is used to provide dependencies throughout the app.

- **`@HiltAndroidApp`**: The `LocalAssistantApp` class is annotated with this to enable Hilt.
- **`@AndroidEntryPoint`**: `MainActivity` is annotated to allow for field injection.
- **`@HiltViewModel`**: ViewModels are annotated to be provided by Hilt.
- **`AppModule.kt`**: A Hilt module is used to provide singleton instances of the `ModelRepository` and `InferenceEngine` based on the user's selection in the settings.

## 4. Llama.cpp Integration

The `llama.cpp` library is integrated as a native inference engine.

- **Git Submodule:** The `llama.cpp` repository is included as a git submodule to keep it up-to-date.
- **JNI Bridge:** A JNI (Java Native Interface) bridge in the `:llama` module connects the Kotlin code with the native C/C++ `llama.cpp` code.
- **`LlamaCppInferenceEngine`**: This class acts as an adapter, wrapping the JNI bridge to conform to the app's `InferenceEngine` interface.

## 5. Data Flow

1.  A **View** (e.g., `ChatScreen`) captures a user action.
2.  The View calls a function in its **ViewModel** (e.g., `ChatViewModel.sendMessage()`).
3.  The **ViewModel**, using dependencies injected by Hilt, calls the appropriate **Repository** or **Inference Engine**.
4.  The **Model** layer performs the business logic (e.g., running inference) and returns the result.
5.  The **ViewModel** updates its state, which is exposed as a `StateFlow`.
6.  The **View**, collecting the `StateFlow`, automatically recomposes to display the updated information.
