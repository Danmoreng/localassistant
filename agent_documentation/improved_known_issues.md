# Known Issues

This document tracks confirmed bugs and limitations within LocalAssistant that require investigation and resolution.

> **Last Updated:** 29 Jul 2025

---

## 🔴 Critical Issues

### Issue #1: Llama.cpp Token Generation Limitation

**Priority:** Critical  
**Status:** Active Investigation  
**Affects:** All llama.cpp inference (Gemma 3B model)  
**First Reported:** 2025-07-28

#### Description
The llama.cpp inference engine severely limits token generation output, typically cutting off responses after 64 tokens. This appears to be a hardcoded limitation within the Android wrapper rather than the core llama.cpp library.

#### Impact
- Responses are incomplete and truncated mid-sentence
- User experience is significantly degraded when using llama.cpp backend
- Makes llama.cpp engine effectively unusable for meaningful conversations

#### Reproduction Steps
1. Switch to llama.cpp engine in settings
2. Send any prompt that would generate > 64 tokens (e.g., "Write a 200-word story")
3. Observe response cuts off abruptly after ~64 tokens

#### Expected vs Actual Behavior
- **Expected:** Full response generation until natural stopping point or max_tokens limit
- **Actual:** Hard cutoff at approximately 64 tokens regardless of context

#### Investigation Notes
- Issue likely originates in `llama_cpp/examples/llama.android` wrapper code
- The core llama.cpp C++ library does not have this limitation
- Android JNI bridge may have artificial constraints in token iteration

#### Workaround
Use ONNX/Phi-4 engine instead of llama.cpp for longer conversations.

#### Resolution Plan
Replace the example Android wrapper with a custom JNI binding (Task S-4 in PROJECT_PLAN.md).

---

## 🟡 High Priority Issues

### Issue #2: Memory Leak During Extended Sessions

**Priority:** High  
**Status:** Under Investigation  
**Affects:** Both ONNX and llama.cpp engines  
**First Reported:** 2025-07-25

#### Description
Memory usage gradually increases during long chat sessions, eventually leading to OOM crashes on devices with < 6GB RAM.

#### Impact
- App crashes after ~2 hours of continuous use
- Affects user retention for extended conversations
- More severe on older/lower-spec Android devices

#### Reproduction Steps
1. Start a new chat session
2. Send 50+ messages with lengthy responses
3. Monitor memory usage via Android Studio profiler
4. Observe steady increase without corresponding decrease

#### Suspected Areas
- Message list not properly releasing old references
- Model context buffers not being cleared
- Compose recomposition holding onto stale state

#### Workaround
Restart the app every hour during extended sessions.

---

### Issue #3: Settings Not Persisting Across App Restarts

**Priority:** High  
**Status:** Fix In Progress (Task B-1)  
**Affects:** All users  
**First Reported:** 2025-07-20

#### Description
User-selected inference engine and custom system prompts reset to defaults when the app is killed and restarted.

#### Impact
- Poor user experience requiring reconfiguration
- Loss of personalized system prompts
- Confusion when engine switches unexpectedly

#### Reproduction Steps
1. Change inference engine from Phi to Llama in settings
2. Modify system prompt text
3. Force-kill the app via task manager
4. Restart app - settings revert to defaults

#### Root Cause
Settings are currently stored in-memory using mutable state rather than persistent storage.

#### Resolution
Implementing DataStore persistence in Task B-1.

---

## 🟠 Medium Priority Issues

### Issue #4: Dark Theme UI Inconsistencies

**Priority:** Medium  
**Status:** Acknowledged  
**Affects:** Users with system dark theme enabled  
**First Reported:** 2025-07-22

#### Description
Several UI components don't properly adapt to dark theme, causing readability issues and visual inconsistencies.

#### Impact
- Poor visual experience for dark theme users
- Text contrast issues affecting accessibility
- Inconsistent theming across different screens

#### Affected Components
- Chat input field border color
- Download progress text visibility
- Settings screen radio button labels
- System prompt text field background

#### Workaround
Use light theme in system settings.

---

### Issue #5: Download Progress Not Showing File Names

**Priority:** Medium  
**Status:** Enhancement Request  
**Affects:** Users downloading models  
**First Reported:** 2025-07-26

#### Description
During model download, the progress indicator shows "Downloading file X of Y" but doesn't display which specific file is being downloaded.

#### Impact
- Users can't track download progress effectively
- No visibility into which files might be failing
- Poor user experience during long downloads

#### Enhancement
Show actual filenames in download progress UI.

---

## 🟢 Low Priority Issues

### Issue #6: Chat Scroll Position Not Preserved

**Priority:** Low  
**Status:** Enhancement Request  
**Affects:** Users with long conversation history  

#### Description
When rotating device or navigating away from chat, scroll position in conversation resets to bottom.

#### Impact
- Minor UX inconvenience when reviewing older messages
- Makes it difficult to reference previous parts of conversation

---

### Issue #7: No Visual Feedback for Message Sending

**Priority:** Low  
**Status:** Enhancement Request  
**Affects:** All users during message composition  

#### Description
After tapping send button, there's no immediate visual feedback that message was received.

#### Impact
- Users might tap send multiple times
- Uncertainty about whether message was processed

#### Enhancement
Add loading state or immediate message bubble appearance.

---

## 📊 Issue Statistics

| Priority | Open | In Progress | Resolved |
|----------|------|-------------|----------|
| Critical | 1 | 0 | 0 |
| High | 2 | 1 | 0 |
| Medium | 2 | 0 | 0 |
| Low | 2 | 0 | 0 |
| **Total** | **7** | **1** | **0** |

---

## 🔧 Reporting New Issues

When reporting new issues, please include:

1. **Device Information:** Model, Android version, RAM
2. **App Version:** Found in Settings > About
3. **Reproduction Steps:** Clear, numbered steps
4. **Expected vs Actual Behavior:** What should happen vs what does happen
5. **Screenshots/Logs:** Visual evidence when applicable
6. **Workarounds:** Any temporary solutions discovered

**Report via:** [GitHub Issues](https://github.com/your-username/localassistant/issues) with the `bug` label.

---

## 📈 Resolution Timeline

### This Sprint (S-4)
- Issue #1: Critical token limitation (Custom JNI binding)
- Issue #3: Settings persistence (DataStore implementation)

### Next Sprint  
- Issue #2: Memory leak investigation and fixes
- Issue #4: Dark theme UI consistency pass

### Future Releases
- Issues #5-7: UX enhancements and quality of life improvements