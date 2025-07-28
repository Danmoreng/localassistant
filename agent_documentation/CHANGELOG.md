# Changelog

This document tracks the major changes, features, and bug fixes implemented in the LocalAssistant app.

---

## [Unreleased]

### Added
- Initial project setup.

## [0.1.0] - 2025-07-28

### Added
- **Engine Switching:** Implemented a settings screen to allow users to switch between the ONNX Runtime and Llama.cpp inference engines.
- **`LlamaCppInferenceEngine`:** A new inference engine that uses the `llama.cpp` library.
- **`LlamaModelRepository`:** A new model repository to handle the download of GGUF models for `llama.cpp`.

### Fixed
- **Startup Crash:** Resolved a crash that occurred when the app tried to load a model before it was downloaded.
- **File Download Error:** Fixed a `FileNotFoundException` by ensuring the parent directory exists before writing a downloaded model to disk.
- **UI State:** Corrected a bug where the UI would not transition from the download screen to the chat screen after a model was successfully downloaded.

### Changed
- **ViewModel Refactoring:** Updated `ChatViewModel` to use a generic `InferenceEngine` interface, decoupling it from a specific implementation.
- **Git Submodule:** The `llama.cpp` library is now included as a git submodule for easier updates.
