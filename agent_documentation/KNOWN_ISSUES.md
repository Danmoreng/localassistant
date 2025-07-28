# Known Issues

This document lists known bugs and issues within the LocalAssistant application that are pending investigation and resolution.

---

## 1. ONNX Inference Context Loss

**Description:** When using the ONNX inference engine, the conversation context is not maintained after the first turn. The initial response to a user query is correct and relevant. However, subsequent responses lose all previous context and appear to be random or unrelated to the ongoing conversation.

**Impact:** The ONNX inference engine is currently unusable for multi-turn conversations.

**Suspected Area:** This likely points to an issue in how the chat history or conversation context is being prepared and passed to the ONNX model, or how the model itself is handling the input sequence for subsequent turns. Relevant files to investigate include `ChatViewModel.kt` (how it manages and passes chat history) and `OnnxInferenceEngine.kt` (how it processes the input for inference).

---

## 2. Llama.cpp Limited Token Generation

**Description:** The `llama.cpp` inference engine appears to have a severely limited token generation output, often cutting off responses prematurely. This suggests an artificial limit on the number of tokens generated per inference call.

**Impact:** The `llama.cpp` engine provides incomplete and truncated responses, significantly hindering its usability for meaningful conversations.

**Suspected Area:** This issue is likely due to a hardcoded limit within the `llama.cpp` Android binding code (specifically within the `llama_cpp` submodule). To resolve this, it may be necessary to re-implement or modify the Android bindings outside of the submodule to allow for custom configuration of the maximum token generation limit, without directly altering the submodule's source code.
