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
