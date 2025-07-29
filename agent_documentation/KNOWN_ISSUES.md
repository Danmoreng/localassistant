# Known Issues

This document tracks confirmed bugs and limitations within the LocalAssistant application.

---

## Critical Issues

### Llama.cpp Inference Token Limit

**Description:**
The llama.cpp inference implementation has a hardcoded `n_len` value, which artificially limits the number of tokens the model can generate in a single response. 

**Impact:**
This causes responses from the Llama 3B model to be cut off prematurely, making it impossible to have extended conversations or generate long-form text.

**Workaround:**
Use the ONNX/Phi-4 engine for conversations requiring longer responses.

**Resolution Plan:**
The hardcoded `n_len` value needs to be replaced with a configurable parameter that can be adjusted by the user or set to a more sensible default.