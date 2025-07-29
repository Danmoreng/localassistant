# LocalAssistant Roadmap

> **Last updated:** 29 Jul 2025

This document outlines the strategic development plan for LocalAssistant, organized by release milestones and feature categories.

---

## 🎯 Current Release: v0.2.0 - Core Stability

**Theme:** Reliable foundation with persistent data and improved UX

### Critical Path Items
- [x] **S-4: Custom JNI Binding** - Replace limited llama.cpp wrapper to unlock full token generation
- [ ] **B-1: Settings Persistence** - DataStore integration for user preferences 
- [ ] **C-1: Context Management** - Prevent crashes on long conversations
- [ ] **D-1: Chat History Storage** - Room database for conversation persistence

### Quality of Life Improvements  
- [ ] **E-1: UI Polish** - Material 3 compliance and visual consistency
- [ ] **Error Handling** - Graceful failure states and user feedback

**Success Metrics:**
- Zero crashes on 8+ hour conversations
- < 3 second app startup time with chat history
- 100% settings persistence across app restarts
- Dark/light theme switching without UI glitches

---

## 🚀 Next Release: v0.3.0 - Enhanced User Experience

**Theme:** Advanced features and model management

### Model Management
- [ ] **Model Browser** - Visual interface for available models with metadata
- [ ] **Background Downloads** - Progress notifications and pause/resume capability  
- [ ] **Storage Management** - Model deletion, space usage, and cleanup tools
- [ ] **Model Switching** - Runtime model swapping without app restart

### Advanced Chat Features
- [ ] **Conversation Management** - Create, rename, delete, and organize chats
- [ ] **Export/Import** - Share conversations or backup chat history
- [ ] **Message Search** - Full-text search across conversation history
- [ ] **Response Regeneration** - Re-roll assistant responses with different parameters

### Performance & Reliability
- [ ] **Memory Optimization** - Efficient model loading and context management
- [ ] **Background Processing** - Continue inference when app is backgrounded
- [ ] **Offline Indicators** - Clear status when models are unavailable
- [ ] **Recovery Mechanisms** - Auto-restart failed inference sessions

**Success Metrics:**
- Support for 3+ concurrent model types (ONNX, GGUF, etc.)
- Background downloads complete 95% of the time
- < 1GB RAM usage during active inference
- Sub-second response time for conversation switching

---

## 🔮 Future Releases: v0.4.0+ - Advanced Capabilities

### Multi-Modal Support (v0.4.0)
- [ ] **Image Understanding** - Vision model integration for image analysis
- [ ] **Voice Input/Output** - Speech-to-text and text-to-speech capabilities
- [ ] **Document Processing** - PDF and text file ingestion and analysis
- [ ] **Code Understanding** - Syntax highlighting and code completion features

### Hardware Acceleration (v0.5.0)
- [ ] **GPU Acceleration** - OpenCL/Vulkan compute backend exploration
- [ ] **NPU Integration** - Neural processing unit support for compatible devices
- [ ] **Quantization Options** - Int4, Int8, FP16 model variants
- [ ] **Batch Processing** - Efficient handling of multiple concurrent requests

---

This document outlines the development plan for the LocalAssistant app. It serves as a high-level guide for current priorities and future ambitions.

---

## 🎯 Next Up: Core Stability & Persistence

Our immediate focus is on hardening the app's foundation. This involves implementing robust data persistence for user settings and chat history, ensuring the app is stable and reliable.

| ID  | Task                                                            | Description                                                                                                |
| --- | --------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| B-1 | **Persist Settings with DataStore**                             | Use Preferences DataStore to save the user's selected engine and system prompt, so they survive app restarts.  |
| C-1 | **Implement Context Safety**                                    | Prevent inference failures on long chats by implementing a simple message-count truncation (e.g., keep last 20). |
| D-1 | **Add Room for Chat History**                                   | Integrate the Room persistence library to store and retrieve chat messages locally.                        |
| D-2 | **Define Message Entity & DAO**                                 | Create the `MessageEntity`, Data Access Object (DAO), and `LocalAssistantDatabase` for Room.               |
| D-3 | **Load History in ViewModel**                                   | Hook the DAO into the `ChatViewModel` to load history on launch and handle chat resets.                    |
| E-1 | **UI Polish Pass**                                              | Align padding, typography, and colors with the Material 3 specification for a consistent look and feel.    |
| E-2 | **Implement Dark Theme**                                        | Ensure the UI looks great in both light and dark modes, respecting the system default setting.             |

---

## 🚀 Future Goals

Once the core experience is stable, we can focus on expanding the app's capabilities and improving the user experience.

- **Model Management:**
    - Implement a model browser to view available models.
    - Add support for background downloads with notifications.
    - Allow users to delete downloaded models.
- **Advanced Features:**
    - Explore and integrate more inference backends (e.g., different model types, hardware acceleration).
    - Implement token-based context truncation for more precise control.
- **Code Quality & Architecture:**
    - Refactor to a more modular architecture with feature modules.
    - Set up CI/CD pipelines with automated linting, static analysis (Detekt), and testing.
    - Conduct performance tuning to optimize resource usage.

