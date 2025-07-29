## S‑4   Replace Example Wrapper with First‑Class JNI Binding  🚧

> **Owner:** “JNI‑Binding” AI agent  
> **Status:** _Planned_  
> **Target branch:** `feature/jni-binding-llama`  
> **Due:** 2025‑08‑05

### 🎯 Goal  
Eliminate the 64‑token limitation and unlock full llama.cpp capabilities by **dropping `examples/llama.android`** and building our own JNI bridge + Kotlin facade.

### 🔨 Deliverables  
| # | Artifact | Location |
|---|----------|----------|
| 1 | **`:llama-binding` library module** (Gradle, Android library) | `/llama-binding` |
| 2 | **CMakeLists.txt** compiling llama.cpp as `libllama-jni.so` | `/llama-binding/src/main/cpp` |
| 3 | **`LlamaJni.cpp`** – minimal C++ wrapper exposing \<10 methods | same |
| 4 | **`Llama.kt`** Kotlin singleton with a `generate()` Flow that accepts `maxTokens` | `/llama-binding/src/main/kotlin` |
| 5 | Updated **settings.gradle.kts** (remove old `:llama` include, add new module) | root |
| 6 | Updated **`LlamaCppInferenceEngine`** pointing to the new API | `app/…/inference` |
| 7 | Instrumentation test that streams ≥ 256 tokens without truncation | `/llama-binding/src/androidTest` |

### 🔍 Acceptance criteria  
- Loading a 2 B‑parameter GGUF on Pixel 8 succeeds.  
- `generate(prompt, maxTokens = 256)` returns a Flow that emits ≥ 256 non‑empty pieces before EOS.  
- Memory usage measured via `meminfo` is ≤ +5 % compared to the old wrapper.  
- ProGuard/R8 keeps JNI symbols (`-keep class com.localassistant.llama.* { *; }`).  
- No code remains under `llama_cpp/examples/llama.android/**/*.kt`.

---

### 🛠️ Detailed task list  

| ID | Task | Description |
|----|------|-------------|
| **S‑4‑1** | **Module bootstrap** | `./gradlew :llama-binding:create` → apply `com.android.library` + `kotlin-android`. |
| **S‑4‑2** | **Copy build flags** | Port NEON/FP16 flags from upstream `CMakeLists.txt` to the new one. |
| **S‑4‑3** | **Add llama subdir** | In CMake: `add_subdirectory(${PROJECT_SOURCE_DIR}/../../llama_cpp libllama EXCLUDE_FROM_ALL)`; link static lib. |
| **S‑4‑4** | **Write `LlamaJni.cpp`** | Expose: `newContext(path)`, `freeContext(ctx)`, `evalTokens(ctx, ids)`, `sample(ctx)`, `tokenEOS(ctx)` (≈150 LOC). |
| **S‑4‑5** | **Kotlin facade** | Singleton that loads `System.loadLibrary("llama-jni")`, owns a background dispatcher, and implements `generate(prompt, maxTokens) : Flow<String>`. |
| **S‑4‑6** | **Engine refactor** | Replace `LLamaAndroid.instance()` calls with the new `Llama` object; add EOS stop condition. |
| **S‑4‑7** | **Gradle cleanup** | Remove `include(":llama")` from settings; delete obsolete `projectDir` line. |
| **S‑4‑8** | **ProGuard/R8 rules** | Keep native classes & fields; disable method inlining for `native` functions. |
| **S‑4‑9** | **Instrumentation test** | UI test sends “Write a 300‑word story” and asserts ≥ 300 words received. |
| **S‑4‑10** | **Docs & sample** | Update README + Architecture docs with new binding diagram. |

---

### ⏱️ Effort & sequencing  

1. **Day 1** – tasks 4‑1 to 4‑4  
2. **Day 2** – tasks 4‑5 & 4‑6  
3. **Day 3** – tasks 4‑7 to 4‑9, code review  
4. **Day 4** – docs, merge

---

### 📓 Notes & tips  

* The C API (`llama.h`) is now stable; prefer it over C++ helpers.  
* Keep the JNI surface *tiny*: pass byte‑arrays of token IDs instead of strings when feeding prompts.  
* You can copy the log‑to‑Android trick from the example (`llama_log_set()` → `__android_log_print`).  
* Use `gradle.properties`: `android.experimental.disableDuplicateClassCheck=true` while transitioning modules.  
* Remember to increment `versionCode` after ABI change.

---

### 🔗 References  

* Upstream discussion on `nlen` issue: ggerganov/llama.cpp #6108  
* Internal design doc: `agent_documentation/deep_dives/LLAMA_CPP.md` (section “Build & Integration Process”)  
* Android NDK r27 – CMake toolchain defaults.
