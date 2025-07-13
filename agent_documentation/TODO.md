# TODO

This document outlines the planned refactoring and improvements for the LocalAssistant app.

## Current Status (Engine-Switching Implemented)

The application now fully supports both **ONNX Runtime** and **Llama.cpp** as selectable inference engines. The integration is complete, and users can switch between them at runtime via a new settings screen. All major bugs identified during the integration process have been resolved.

## Integration & Bug Fix Checklist

Here is the breakdown of the completed integration and bug-fixing steps:

- [x] **Add `llama.cpp` as a Git Submodule:** Added the official `llama.cpp` repository as a git submodule.
- [x] **Integrate `:llama` Android Library:** Configured `settings.gradle.kts` and `app/build.gradle.kts` to include the `:llama` module from the submodule.
- [x] **Create `LlamaCppInferenceEngine`:** Implemented the `LlamaCppInferenceEngine` class, which conforms to the `InferenceEngine` interface.
- [x] **Create `LlamaModelRepository`:** Implemented the `LlamaModelRepository` to manage the download of GGUF models.
- [x] **Refactor ViewModels:** Updated `ChatViewModel` to use the generic `InferenceEngine` interface, decoupling it from any specific implementation.
- [x] **Resolve Build Issues:** Fixed various compilation errors related to dependency injection, constructor mismatches, and interface definitions.
- [x] **Fix Runtime Crash on Startup:** Resolved a crash caused by the app trying to load a model before it was downloaded. The fix involved deferring the `ChatViewModel` creation until after the model's existence is verified.
- [x] **Fix File Download Error:** Corrected an `ENOENT (No such file or directory)` error by ensuring the parent directory is created before writing a downloaded model file to disk.
- [x] **Fix Download Screen State:** Fixed a bug where the UI would not automatically transition from the download screen to the chat screen upon completion. This was resolved by correctly updating the `isModelAvailable` state in the `DownloadViewModel`.
- [x] **Implement Engine-Switching UI:**
    - Created an `Engine` enum to represent the available backends.
    - Added a `SettingsScreen` to allow users to select their preferred engine.
    - Added a settings icon to the `ChatScreen` to provide access to the `SettingsScreen`.
    - Updated `MainActivity` to manage the engine selection state and dynamically recreate the ViewModels with the correct dependencies when the engine is changed.

## Future Goals

*   Implement a proper Dependency Injection framework (e.g., Hilt, Koin) to replace the manual injection in `MainActivity`.
*   Continue with modularization by creating feature modules.
*   Explore adding more inference backends.
