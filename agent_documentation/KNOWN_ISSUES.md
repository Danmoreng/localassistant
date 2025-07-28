# Known Issues

This document lists known bugs and issues within the LocalAssistant application that are pending investigation and resolution.

---



## 2. Llama.cpp Limited Token Generation

**Description:** The `llama.cpp` inference engine appears to have a severely limited token generation output, often cutting off responses prematurely. This suggests an artificial limit on the number of tokens generated per inference call.

**Impact:** The `llama.cpp` engine provides incomplete and truncated responses, significantly hindering its usability for meaningful conversations.

**Suspected Area:** This issue is likely due to a hardcoded limit within the `llama.cpp` Android binding code (specifically within the `llama_cpp` submodule). To resolve this, it may be necessary to re-implement or modify the Android bindings outside of the submodule to allow for custom configuration of the maximum token generation limit, without directly altering the submodule's source code.
