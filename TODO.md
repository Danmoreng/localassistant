# TODO

This document outlines the planned refactoring and improvements for the LocalAssistant app.

## 1. Modularization

The current app structure is monolithic, with all the code in the `app` module. To improve maintainability, testability, and prepare for future features, we will modularize the app.

**Plan:**

1.  **Create a `core` module:** This module will contain the core business logic of the app, independent of the Android framework. It will include:
    *   Data models (e.g., `Message`, `Model`)
    *   Repository interfaces (e.g., `ChatRepository`, `DownloadRepository`)
    *   Inference engine abstractions (see below)

2.  **Create a `feature` module:** This module will contain the UI and ViewModel for a specific feature, such as the chat screen. This will help to decouple the features from each other and from the main app module.

3.  **Update the `app` module:** The `app` module will be responsible for dependency injection, navigation, and bringing all the modules together.

## 2. Inference Engine Abstraction

To support multiple inference engines (e.g., ONNX, llama.cpp), we need to abstract the inference logic.

**Plan:**

1.  **Create an `InferenceEngine` interface:** This interface will define a common set of methods for all inference engines, such as `generateResponse()`.

2.  **Create an `OnnxInferenceEngine` implementation:** This class will implement the `InferenceEngine` interface and contain the existing ONNX-specific logic.

3.  **Create a `LlamaCppInferenceEngine` implementation (future):** This class will implement the `InferenceEngine` interface and contain the llama.cpp-specific logic.

4.  **Update the `InferenceRepository`:** The `InferenceRepository` will be responsible for selecting the active inference engine and delegating the work to it.

## 3. Refactoring Plan

We will tackle the refactoring in the following order:

1.  **Create the `core` module.**
2.  **Move the data models and repository interfaces to the `core` module.**
3.  **Create the `InferenceEngine` interface and refactor the existing ONNX logic.**
4.  **Create the `feature` module and move the chat screen to it.**
5.  **Update the `app` module to use the new modules.**
6.  **Implement the `LlamaCppInferenceEngine` (future).**
