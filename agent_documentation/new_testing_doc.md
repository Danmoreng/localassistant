# Testing Strategy

> **Last Updated:** 29 Jul 2025

This document outlines the comprehensive testing approach for LocalAssistant, covering unit tests, integration tests, and manual testing procedures.

---

## 🎯 Testing Objectives

- **Reliability:** Ensure inference engines work correctly across different models and devices
- **Performance:** Validate memory usage, response times, and battery impact
- **User Experience:** Test UI flows, error handling, and edge cases
- **Compatibility:** Verify functionality across Android versions and device configurations

---

## 🏗️ Testing Architecture

### Test Pyramid Structure

```
        E2E Tests (5%)
       ┌─────────────────┐
      │  UI/Integration   │
     └─────────────────────┘
    
    Integration Tests (20%)
   ┌─────────────────────────┐
  │  Repository & ViewModel  │
 └───────────────────────────┘

      Unit Tests (75%)
┌─────────────────────────────────┐
│  Models, Utils, Business Logic  │
└─────────────────────────────────┘
```

---

## 🧪 Unit Tests

### Coverage Targets
- **Minimum:** 80% line coverage
- **Target:** 90% line coverage for core business logic
- **Critical Paths:** 100% coverage for inference engines and data repositories

### Test Categories

#### Model Classes (`/model`)
```kotlin
// Example: MessageTest.kt
@Test
fun `TextMessage should update text property reactively`() {
    val message = TextMessage("initial", MessageType.USER)
    message.text = "updated"
    assertEquals("updated", message.text)
}
```

#### Repository Logic (`/data`)
```kotlin
// Example: Phi4ModelRepositoryTest.kt
@Test
fun `isModelAvailable returns false when model files missing`() = runTest {
    // Given: Empty model directory
    // When: Check availability
    // Then: Should return false
}
```

#### Inference Engines (`/inference`)
```kotlin
// Example: OnnxInferenceEngineTest.kt
@Test
fun `formatChat should properly structure conversation with system prompt`() {
    // Test chat formatting logic
}
```

### Running Unit Tests
```bash
./gradlew test
./gradlew testDebugUnitTest --tests="*ModelRepository*"
```

---

## 🔗 Integration Tests

### Repository Integration
Test repository classes with real file I/O and network operations:

```kotlin
@RunWith(AndroidJUnit4::class)
class ModelDownloaderIntegrationTest {
    
    @Test
    fun downloadSingleFile_shouldCreateFileWithCorrectContent() = runTest {
        // Test actual file download and verification
    }
}
```

### Database Integration
Test Room database operations:

```kotlin
@RunWith(AndroidJUnit4::class)
class ChatHistoryDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: LocalAssistantDatabase
    private lateinit var chatDao: ChatHistoryDao
    
    @Test
    fun insertAndRetrieveMessage_shouldPersistCorrectly() = runTest {
        // Test database operations
    }
}
```

### ViewModel Integration
Test ViewModels with mock repositories:

```kotlin
@ExperimentalCoroutinesApi
class ChatViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun sendMessage_shouldAddToMessagesAndTriggerResponse() = runTest {
        // Test ViewModel behavior with mocked dependencies
    }
}
```

---

## 📱 UI Tests (Instrumentation)

### Test Scenarios

#### Critical User Flows
1. **First Launch & Model Download**
   ```kotlin
   @Test
   fun firstLaunch_shouldShowDownloadScreen_andNavigateToChat() {
       // Test complete onboarding flow
   }
   ```

2. **Chat Functionality**
   ```kotlin
   @Test
   fun sendMessage_shouldDisplayInChatAndReceiveResponse() {
       // Test end-to-end chat interaction
   }
   ```

3. **Settings Management**
   ```kotlin
   @Test
   fun changeInferenceEngine_shouldPersistAcrossAppRestarts() {
       // Test settings persistence
   }
   ```

#### Edge Cases
- Network interruption during model download
- Low memory conditions during inference
- Background/foreground transitions during chat
- Device rotation during active response generation

### Test Utilities
```kotlin
// Custom test rules for common setup
class InferenceEngineTestRule : TestWatcher() {
    override fun starting(description: Description?) {
        // Setup mock inference engine
    }
}

// Page Object Model for UI tests
class ChatScreenRobot {
    fun typeMessage(text: String) = apply {
        onView(withId(R.id.message_input)).perform(typeText(text))
    }
    
    fun tapSend() = apply {
        onView(withId(R.id.send_button)).perform(click())
    }
    
    fun assertMessageDisplayed(text: String) {
        onView(withText(text)).check(matches(isDisplayed()))
    }
}
```

---

## 🚀 Performance Tests

### Benchmarks
Using Android's Macrobenchmark library:

```kotlin
@RunWith(AndroidJUnit4::class)
class InferenceBenchmark {
    
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun measureInferenceLatency() {
        benchmarkRule.measureRepeated(
            packageName = "com.localassistant",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5
        ) {
            // Simulate inference workload
        }
    }
}
```

### Memory Tests
```kotlin
@Test
fun longConversation_shouldNotExceedMemoryThreshold() {
    // Generate 100+ message conversation
    // Assert max memory usage < 2GB
    // Check for memory leaks
}
```

---

## 🔧 Manual Testing Procedures

### Device Compatibility Matrix

| Test Scenario | Pixel 6 | Samsung S21 | OnePlus 9 | Budget Device |
|---------------|---------|-------------|-----------|---------------|
| Model Download | ✅ | ✅ | ✅ | ⏳ |
| ONNX Inference | ✅ | ✅ | ✅ | ❌ |
| Llama.cpp Inference | ✅ | ✅ | ⚠️ | ❌ |
| Extended Sessions | ✅ | ✅ | ⚠️ | ❌ |

**Legend:** ✅ Pass | ⚠️ Issues | ❌ Fail | ⏳ Pending

### Test Checklists

#### Pre-Release Checklist
- [ ] All automated tests pass
- [ ] App starts successfully on 5 different devices
- [ ] Model downloads complete successfully
- [ ] Both inference engines generate coherent responses
- [ ] Settings persist across app restarts
- [ ] No memory leaks during 30-minute sessions
- [ ] Dark/light theme switching works correctly
- [ ] Proper error handling for network issues

#### Performance Validation
- [ ] Cold start time < 5 seconds
- [ ] Model loading time < 30 seconds (7B model)
- [ ] First response time < 10 seconds
- [ ] Memory usage stable over 1-hour session
- [ ] Battery drain < 5% per 30-minute session

---

## 🤖 Automated Testing Pipeline

### CI/CD Integration

```yaml
# .github/workflows/tests.yml
name: Tests
on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run unit tests
        run: ./gradlew test
      
  instrumentation-tests:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew connectedAndroidTest
```

### Test Reporting
- **Coverage Reports:** JaCoCo integration with GitHub Pages
- **Performance Tracking:** Benchmark results stored in Firebase
- **Flaky Test Detection:** Automated retry and reporting
- **Test Distribution:** Run on multiple API levels and architectures

---

## 📊 Test Metrics & KPIs

### Quality Gates
- **