# LocalAssistant 🤖

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A private, on-device AI chat application for Android. LocalAssistant runs large language models (LLMs) directly on your phone, ensuring that your conversations remain completely private and functional even without an internet connection.

<!-- TODO: Add a screenshot or GIF of the app in action -->
<!-- <p align="center">
  <img src="media/app-demo.gif" width="300" />
</p> -->

---

## ✨ Features

- **100% On-Device:** All processing happens on your device. Your data never leaves your phone.
- **Offline Capable:** Works perfectly without an internet connection.
- **Multiple Backends:** Supports different inference engines, including:
    - **ONNX Runtime:** For models in the `.onnx` format.
    - **Llama.cpp:** For models in the `.gguf` format.
- **Clean & Modern UI:** Built with Jetpack Compose, following Material 3 design guidelines.
- **Chat History:** Saves your conversations locally for future reference.
- **Customizable:** Allows you to change the system prompt to tailor the assistant's behavior.

## 🏗️ Architecture

The app is built using modern Android development practices, following an MVVM (Model-View-ViewModel) architecture.

- **UI:** Jetpack Compose for a declarative and reactive UI.
- **Dependency Injection:** Hilt for managing dependencies and decoupling components.
- **Asynchronous Operations:** Kotlin Coroutines and Flow for managing background tasks and data streams.

For a detailed breakdown of the project's structure, components, and data flow, please see the [**ARCHITECTURE.md**](agent_documentation/ARCHITECTURE.md) file.

## 🚀 Getting Started

To build and run the project, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/localassistant.git
    cd localassistant
    ```

2.  **Initialize the Git Submodules:**
    This project uses `llama.cpp` as a git submodule. Initialize it with the following command:
    ```bash
    git submodule update --init --recursive
    ```

3.  **Open in Android Studio:**
    Open the project in the latest stable version of Android Studio.

4.  **Build and Run:**
    Let Android Studio sync the Gradle files, then build and run the app on an emulator or a physical device. The app will automatically download a default model on the first launch.

## 🗺️ Roadmap

We have a clear plan for future features and improvements, including a model browser, background downloads, and further performance optimizations.

Check out our [**ROADMAP.md**](agent_documentation/ROADMAP.md) to see what's next.

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
