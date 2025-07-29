# LocalAssistant Project Plan

> **Last Updated:** 29 Jul 2025

This document outlines the development roadmap for LocalAssistant, organized by sprints and priority levels.

---

## 🎯 Current Sprint: Core Stability & JNI Enhancement

### S-4: Replace Example Wrapper with First-Class JNI Binding 🚧

> **Owner:** "JNI-Binding" AI agent  
> **Status:** _In Progress_  
> **Target branch:** `feature/jni-binding-llama`  
> **Due:** 2025-08-05  
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
**Effort:** 6h  
**Description:** Implement Preferences DataStore to persist user's selected inference engine and system prompt across app restarts.

**Tasks:**
- Create `UserPreferencesRepository` using DataStore
- Migrate `SettingsRepositoryImpl` to use DataStore instead of in-memory storage
- Add migration logic for existing users
- Update `ChatViewModel` to load persisted system prompt on startup

### C-1: Implement Context Safety  
**Priority:** High  
**Effort:** 8h  
**Description:** Prevent inference failures on long conversations by implementing intelligent message truncation.

**Tasks:**
- Implement sliding window context management (keep last 20 messages)
- Add token counting logic to respect model context limits
- Create context overflow warning UI component
- Add unit tests for context management logic

### D-1: Add Room for Chat History
**Priority:** Medium  
**Effort:** 12h  
**Description:** Integrate Room persistence library to store and retrieve chat messages locally.

**Tasks:**
- Define `MessageEntity`, `ConversationEntity` with Room annotations
- Create `MessageDao` and `ConversationDao` with CRUD operations
- Implement `LocalAssistantDatabase` with proper migrations
- Update `ChatViewModel` to use persistent storage
- Add conversation management (create, delete, rename conversations)

### E-1: UI Polish Pass
**Priority:** Medium  
**Effort:** 10h  
**Description:** Align UI components with Material 3 specification for consistent design.

**Tasks:**
- Audit all components against Material 3 guidelines
- Standardize padding, margins, and typography scales
- Implement proper elevation and shadow usage
- Add smooth transitions between screens
- Optimize layout for different screen sizes

### E-2: Implement Dark Theme
**Priority:** Low  
**Effort:** 6h  
**Description:** Support both light and dark themes, respecting system preferences.

**Tasks:**
- Define dark color palette following Material 3 guidelines
- Update all components to use theme-aware colors
- Test contrast ratios for accessibility compliance
- Add theme toggle option in settings (optional)

---

## 🚀 Future Releases

### Model Management Features
- Model browser with download progress and metadata
- Background downloads with notification support
- Model deletion and storage management
- Multiple model support with switching capabilities

### Advanced Inference Features
- Hardware acceleration exploration (GPU, NPU)
- Quantization options and model optimization
- Custom stopping criteria and generation parameters
- Batch inference support for multiple requests

### Code Quality & DevOps
- Modular architecture with feature modules
- CI/CD pipeline with automated testing
- Static analysis integration (Detekt, ktlint)
- Performance monitoring and crash reporting

---

## 📊 Sprint Velocity Tracking

| Sprint | Planned (hours) | Completed (hours) | Velocity |
|--------|----------------|-------------------|----------|
| S-4 (Current) | 36 | TBD | TBD |
| Previous | - | - | - |

---

## 🔗 References

- [Android NDK CMake Guide](https://developer.android.com/ndk/guides/cmake)
- [llama.cpp C API Documentation](https://github.com/ggerganov/llama.cpp/blob/master/llama.h)
- [Material 3 Design Guidelines](https://m3.material.io/)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)