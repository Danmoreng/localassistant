# TODO

This document outlines the planned refactoring and improvements for the LocalAssistant app.

## 1. Modularization

The current app structure is being modularized to improve maintainability, testability, and prepare for future features.

**Progress:**

*   **`core/engine` module moved to `app/src/main/java/com/localassistant/engine`:** The `InferenceEngine` interface and `Engine.kt` have been moved to the `app` module for now. A dedicated `core` module for general business logic might be considered later if more shared components emerge.

**Remaining Plan:**

1.  **Create a `feature` module:** This module will contain the UI and ViewModel for a specific feature, such as the chat screen. This will help to decouple the features from each other and from the main app module.
2.  **Update the `app` module:** The `app` module will be responsible for dependency injection, navigation, and bringing all the modules together. (Manual DI is currently implemented, but a proper DI framework is a future task).

## 2. Inference Engine Abstraction

The inference logic has been abstracted to support multiple inference engines (e.g., ONNX, llama.cpp).

**Progress:**

1.  **`InferenceEngine` interface created and modified:** This interface defines a common set of methods for all inference engines, including streaming support with `Flow`.
2.  **`OnnxInferenceEngine` implementation created:** This class implements the `InferenceEngine` interface and contains the ONNX-specific logic.
3.  **`ModelRepository` interface created and implemented by `Phi4ModelRepository`:** This abstracts model downloading and management.

**Remaining Plan:**

1.  **Create a `LlamaCppInferenceEngine` implementation (future):** This class will implement the `InferenceEngine` interface and contain the llama.cpp-specific logic.
2.  **Update the `InferenceRepository`:** The `InferenceRepository` will be responsible for selecting the active inference engine and delegating the work to it. (This is implicitly handled by manual DI in `MainActivity` for now).

## 3. Refactoring Plan (Completed Steps)

*   **Created the `InferenceEngine` interface and refactored the existing ONNX logic.**
*   **Created the `ModelRepository` interface and refactored `Phi4ModelRepository` to implement it.**
*   **Updated `ChatViewModel` and `DownloadViewModel` to use the new interfaces.**
*   **Updated `MainActivity` for manual dependency injection.**

**Next Steps:**

*   Implement a proper Dependency Injection framework (e.g., Hilt, Koin).
*   Continue with modularization by creating feature modules.
*   Implement additional inference engines (e.g., LlamaCppInferenceEngine).