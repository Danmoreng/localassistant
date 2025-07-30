# LocalAssistant Project Plan

> **Last Updated:** 29 Jul 2025

This document outlines the development roadmap for LocalAssistant, organized by sprints and priority levels.

---

## 🎯 Current Sprint: Core Stability & JNI Enhancement

### S-4: Replace Example Wrapper with First-Class JNI Binding 🚧

> **Owner:** "JNI-Binding" AI agent  
> **Status:** _In Progress_
> **Priority:** Critical

#### 🎯 Goal  
Eliminate the 64-token limitation and unlock full llama.cpp capabilities by **dropping `examples/llama.android`** and building our own JNI bridge + Kotlin facade.

#### 🔨 Deliverables  
| # | Artifact | Location | Status |
|---|----------|----------|---------|
| 1 | **`:llama-binding` library module** (Gradle, Android library) | `/llama-binding` | ⏳ Pending |
| 2 | **CMakeLists.txt** compiling llama.cpp as `libllama-jni.so` | `/llama-binding/src/main/cpp` | ⏳ Pending |
| 3 | **`LlamaJni.cpp`** – C++ wrapper with core inference functions | same | ⏳ Pending |
| 4 | **`Llama.kt`** Kotlin singleton with streaming `generate()` method | `/llama-binding/src/main/kotlin` | ⏳ Pending |
| 5 | Updated **settings.gradle.kts** (remove old `:llama` include, add new module) | root | ⏳ Pending |
| 6 | Updated **`LlamaCppInferenceEngine`** pointing to the new API | `app/…/inference` | ⏳ Pending |
| 7 | Instrumentation test validating ≥256 token generation | `/llama-binding/src/androidTest` | ⏳ Pending |

#### 🔍 Acceptance Criteria  
- [ ] Loading a 2B-parameter GGUF on Pixel 8 succeeds
- [ ] `generate(prompt, maxTokens = 256)` returns a Flow that emits ≥256 non-empty tokens before EOS
- [ ] Memory usage is ≤ +5% compared to the old wrapper (measured via `meminfo`)
- [ ] ProGuard/R8 keeps JNI symbols (`-keep class com.localassistant.llama.* { *; }`)
- [ ] No code remains under `llama_cpp/examples/llama.android/**/*.kt`

---

### 🛠️ Detailed Task Breakdown

| ID | Task | Description | Effort | Dependencies |
|----|------|-------------|---------|--------------|
| **S-4-1** | **Module Bootstrap** | Create `:llama-binding` module with `./gradlew :llama-binding:create` → apply `com.android.library` + `kotlin-android` plugins | 2h | None |
| **S-4-2** | **CMake Configuration** | Port NEON/FP16 optimization flags from upstream `CMakeLists.txt`; configure NDK toolchain | 4h | S-4-1 |
| **S-4-3** | **Llama Submodule Integration** | Add `add_subdirectory(${PROJECT_SOURCE_DIR}/../../llama_cpp libllama EXCLUDE_FROM_ALL)` in CMake; link static library | 3h | S-4-2 |
| **S-4-4** | **Implement LlamaJni.cpp** | C++ JNI wrapper exposing: `newContext(path)`, `freeContext(ctx)`, `evalTokens(ctx, ids)`, `sample(ctx)`, `tokenEOS(ctx)` (~150 LOC) | 8h | S-4-3 |
| **S-4-5** | **Kotlin Facade Development** | Create singleton with `System.loadLibrary("llama-jni")`, background dispatcher, and `generate(prompt, maxTokens): Flow<String>` | 6h | S-4-4 |
| **S-4-6** | **Engine Integration** | Replace `LLamaAndroid.instance()` calls with new `Llama` object; implement EOS stop condition | 4h | S-4-5 |
| **S-4-7** | **Gradle Cleanup** | Remove `include(":llama")` from settings.gradle.kts; delete obsolete `projectDir` configuration | 1h | S-4-6 |
| **S-4-8** | **ProGuard/R8 Rules** | Add keep rules for native classes & fields; disable method inlining for `native` functions | 2h | S-4-7 |
| **S-4-9** | **Instrumentation Testing** | UI test: send "Write a 300-word story" and assert ≥300 words received without truncation | 4h | S-4-8 |
| **S-4-10** | **Documentation Update** | Update README + ARCHITECTURE.md with new binding architecture diagram | 2h | S-4-9 |

**Total Estimated Effort:** 36 hours (4.5 days)

---

## 📋 Next Sprint: Data Persistence & UI Polish

### B-1: Persist Settings with DataStore
**Priority:** High  
**Description:** Implement Preferences DataStore to persist user's selected inference engine and system prompt across app restarts.

**Tasks:**
- Create `UserPreferencesRepository` using DataStore
- Migrate `SettingsRepositoryImpl` to use DataStore instead of in-memory storage
- Add migration logic for existing users
- Update `ChatViewModel` to load persisted system prompt on startup

### C-1: Implement Context Safety  
**Priority:** High
**Description:** Prevent inference failures on long conversations by implementing intelligent message truncation.

**Tasks:**
- Implement sliding window context management (keep last 20 messages)
- Add token counting logic to respect model context limits
- Create context overflow warning UI component
- Add unit tests for context management logic

### D-1: Add Room for Chat History
**Priority:** Medium
**Description:** Integrate Room persistence library to store and retrieve chat messages locally.

**Tasks:**
- Define `MessageEntity`, `ConversationEntity` with Room annotations
- Create `MessageDao` and `ConversationDao` with CRUD operations
- Implement `LocalAssistantDatabase` with proper migrations
- Update `ChatViewModel` to use persistent storage
- Add conversation management (create, delete, rename conversations)

### E-1: UI Polish Pass
**Priority:** Medium
**Description:** Align UI components with Material 3 specification for consistent design.

**Tasks:**
- Audit all components against Material 3 guidelines
- Standardize padding, margins, and typography scales
- Implement proper elevation and shadow usage
- Add smooth transitions between screens
- Optimize layout for different screen sizes

---

### 📓Notes & tips  

* The C API (`llama.h`) is now stable; prefer it over C++ helpers.  
* Keep the JNI surface *tiny*: pass byte‑arrays of token IDs instead of strings when feeding prompts.  
* You can copy the log‑to‑Android trick from the example (`llama_log_set()` → `__android_log_print`).  
* Use `gradle.properties`: `android.experimental.disableDuplicateClassCheck=true` while transitioning modules.  
* Remember to increment `versionCode` after ABI change.

---

### 🔗References  

* Upstream discussion on `nlen` issue: ggerganov/llama.cpp #6108  
* Internal design doc: `agent_documentation/deep_dives/LLAMA_CPP.md` (section “Build & Integration Process”)  
* Android NDK r27 – CMake toolchain defaults.

Below is a structured “ before / after ” comparison, followed by the concrete reasons your **new commit** fails to emit tokens and why the **engine‑switching UX** no longer works, plus step‑by‑step fixes.

---

## 1 .  Architecture level changes

| Area                  | Old (commit **working**)                                                                                                               | New (commit **broken**)                                                                                                                                                                         |
| --------------------- | -------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **llama.cpp wrapper** | Re‑used the upstream sample module `examples/llama.android` exposed through the Kotlin facade `LLamaAndroid` (hard‑coded `nlen = 64`)  | Replaced by your own `:llama-binding` Android‑library module that builds `llamacpp_jni` via CMake and exposes a new Kotlin class `Llama`                                                        |
| **Gradle setup**      | `settings.gradle.kts` includes `:llama` and points at the sample project                                                               | `:llama` removed; new `:llama-binding` added and `app` declares `implementation(project(":llama-binding"))`                                                                                     |
| **Inference engine**  | `LlamaCppInferenceEngine` delegates to **LLamaAndroid** and offers<br>`fun generateResponse(prompt): Flow<String>` (non‑suspending)    | Re‑implemented to wrap the new **Llama** facade and changed the API to<br>`suspend fun generateResponse(…)`                                                                                     |
| **DI / ViewModel**    | `ChatViewModel` *listens* to `SettingsRepository.selectedEngine` and builds either ONNX or llama.cpp engines at runtime                | Hilt now injects a *single* `InferenceEngine` that `InferenceModule` always wires to `LlamaCppInferenceEngine` ; the new `ChatViewModel` therefore never looks at `SettingsRepository` anymore  |

---

## 2 .  Why **no tokens are emitted**

### 2·1 Native bug — `tokenToPiece`

In `llamacpp_bridge.cpp` you call `llama_token_to_piece` with a zero‑length buffer to obtain the required size, but you treat **non‑negative** return values as an error:

```cpp
int n = llama_token_to_piece(vocab, token, nullptr, 0, false, false);
if (n >= 0) {              // <-- wrong: n is the required size!
    return env->NewByteArray(0);
}
int required_size = -n;    // <-- becomes negative
```

Because the early‑return path always triggers, every sampled token is converted to an **empty byte‑array**, so `Llama.generate()` only emits empty strings to the UI .

> **Fix**

```cpp
int n = llama_token_to_piece(vocab, token, nullptr, 0, false, false);
if (n <= 0) {
    return env->NewByteArray(0); // error
}

std::vector<char> out(n);
int written = llama_token_to_piece(vocab, token, out.data(), n, false, false);
if (written <= 0) {
    return env->NewByteArray(0);
}

jbyteArray result = env->NewByteArray(written);
env->SetByteArrayRegion(result, 0, written, reinterpret_cast<jbyte*>(out.data()));
return result;
```

This alone lets the Kotlin layer receive real UTF‑8 slices.

### 2·2 Kotlin side — decoding & flow

`Llama.generate()` already converts the returned `ByteArray` to a `String` and emits it via `Flow` . Once the native bug is fixed, the UI will start seeing text without further changes.

---

## 3 .  Why engine switching broke

1. **InferenceModule is hard‑wired to llama.cpp** – it always returns `LlamaCppInferenceEngine`, ignoring the stored preference .
2. **ChatViewModel no longer watches `SettingsRepository`** and simply uses the injected engine , whereas the old version rebuilt the engine whenever the preference changed .
3. **DownloadViewModel still uses the preference**, so the two ViewModels are now out of sync: the download screen may fetch an ONNX model, but the chat screen keeps using llama.cpp.

> **Fix (minimal)**
>
> ```kotlin
> @Module
> @InstallIn(SingletonComponent::class)
> object InferenceModule {
>     @Provides
>     fun provideInferenceEngine(
>         @ApplicationContext ctx: Context,
>         settings: SettingsRepository
>     ): InferenceEngine = runBlocking {
>         when (settings.selectedEngine.first()) {
>             "phi"   -> OnnxInferenceEngine(ctx, Phi4ModelRepository(ctx, ModelDownloader()).getModelDirectory().absolutePath)
>             "llama" -> LlamaCppInferenceEngine(LlamaModelRepository(ctx, ModelDownloader()).getModelPath())
>             else    -> LlamaCppInferenceEngine("error‑path")
>         }
>     }
> }
> ```
>
> *or* revert to the former pattern where `ChatViewModel` builds the engine itself and listens to the flow.

---

## 4 .  Token‑limit elimination

The new pipeline can now stream up to `maxTokens` (default **256**) passed from `LlamaCppInferenceEngine.generateResponse()` .  After the `tokenToPiece` fix you will have removed the 64‑token ceiling of the sample wrapper.

---

## 5 .  Recommended clean‑up checklist

| Area              | Action                                                                                                                          |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| **JNI**           | ✓ Fix `tokenToPiece` return‑code logic<br>✓ Guard against invalid UTF‑8 (similar to the sample’s `is_valid_utf8`)               |
| **Kotlin API**    | ✓ Change `generateResponse` back to `fun` (non‑suspending) or update *all* call‑sites to use a suspending function consistently |
| **DI / Settings** | ✓ Either provide an `EngineFactory` that selects at runtime, or restore the old ViewModel logic                                 |
| **Testing**       | ✓ Add an instrumentation test that asks for ≥300 tokens and asserts the length to prevent regressions (already on your roadmap) |
| **Build**         | ✓ Add R8 keep rules for the new native class names (`-keep class com.example.localassistant.llamacpp.** { *; }`)                |
| **Docs**          | ✓ Update README / deep‑dive docs to mention the new module and removal of the sample wrapper                                    |

---

### Bottom line

* **No output** stems from one wrong comparison operator in `tokenToPiece`.
* **Engine switching** stopped working because the DI graph now binds a single llama.cpp engine and `ChatViewModel` no longer reacts to the preference.

Correcting those two areas should restore functionality **and** give you the desired >64‑token capability.

