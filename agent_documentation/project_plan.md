# LocalAssistant ‑ **Rapid‑Ship Plan**

> **Last updated:** 27 Jul 2025

---

## 1 · Objective

Deliver a **stable, good‑looking on‑device LLM chat app** as quickly as possible.  We keep the scope tight: fix architectural pain points, remember user choices, avoid model crashes, and ship with a polished Compose UI.  All other ambitions move to a *Later / Backlog* section.

---

## 2 · Core Principles

1. **Stability first** → The app must not crash or lose user data.
2. **Essential persistence** → Remember engine choice, system prompt, and recent chat.
3. **Beautiful but lean UI** → Jetpack Compose components stay modular and theme‑consistent—no extra screens until core flows are smooth.
4. **Incremental upgrades** → Each milestone compiles, runs, and can be demoed.

---

## 3 · High‑Level Goals (v 0.1)

|  ID  | Goal                        | Ship When …                                                           |
| ---- | --------------------------- | --------------------------------------------------------------------- |
|  G1  | 💉 **Hilt DI**              | Manual factories are gone; changing engine in Settings reinjects VMs. |
|  G2  | 💾 **Settings Persistence** | Engine + system prompt survive restarts using Preferences DataStore.  |
|  G3  | 🛑 **Context Safety**       | Chat prompt never exceeds model context (simple truncation).          |
|  G4  | 🗂 **Chat History**         | Messages stored locally with Room and restored on launch.             |
|  G5  | ✨ **UI Polish Pass**        | Consistent Material 3 theming; basic dark‑mode and spacing tweaks.    |

---

## 4 · Milestones & Tasks

### Milestone A · "Foundation Stability" (week 1‑2)

|  #   | Task                                                                                 | DoD                                                                        |
| ---- | ------------------------------------------------------------------------------------ | -------------------------------------------------------------------------- |
|  A‑1 | **Add Hilt dependencies & plugins**                                                  | `build.gradle.kts` builds; `Application` annotated with `@HiltAndroidApp`. |
|  A‑2 | **Inject ViewModels** using `@HiltViewModel`                                         | No more custom factories in `MainActivity.kt`.                             |
|  A‑3 | **AppModule.kt** provides `ModelRepository`, `InferenceEngine`, `SettingsRepository` | Switching engine updates DI graph instantly.                               |

### Milestone B · "Remember Me" (week 2‑3)

|  #   | Task                                                            | DoD                                                 |
| ---- | --------------------------------------------------------------- | --------------------------------------------------- |
|  B‑1 | **Preferences DataStore** for `selectedEngine` & `systemPrompt` | Values restored on cold start.                      |
|  B‑2 | **SettingsRepository** wrapper + Hilt binding                   | ViewModels call suspend fun to read/write settings. |

### Milestone C · "Context Guard" (week 3)

|  #   | Task                                                                      | DoD                                      |
| ---- | ------------------------------------------------------------------------- | ---------------------------------------- |
|  C‑1 | **Implement message‑count truncation** (keep last N messages, default 20) | No inference failure after lengthy chat. |
|  C‑2 | (Optional) Token‑count truncation using Phi‑4 tokenizer                   | Unit test proves ≤ 4096 tokens.          |

### Milestone D · "Chat Persistence" (week 3‑4)

|  #   | Task                                                  | DoD                                               |
| ---- | ----------------------------------------------------- | ------------------------------------------------- |
|  D‑1 | **Add Room dependencies**                             | Project compiles.                                 |
|  D‑2 | Define `MessageEntity`, DAO, `LocalAssistantDatabase` | Insert & retrieve messages.                       |
|  D‑3 | Hook DAO into `ChatViewModel`                         | History loads on launch; Reset Chat clears table. |

### Milestone E · "UI Polish v1" (week 4‑5)

|  #   | Task                                                       | DoD                                              |
| ---- | ---------------------------------------------------------- | ------------------------------------------------ |
|  E‑1 | Align padding, typography, and colors with Material 3 spec | Visual pass accepted by designer (or yourself!). |
|  E‑2 | Add dark‑theme preview & switch (system default)           | Looks good in both modes.                        |

---

## 5 · Backlog (post‑v0.1)

- Model browser & background download notifications
- Domain/use‑case layer refactor
- Gradle modularisation & build time cuts
- Automated CI, lint, and Detekt baseline
- Advanced performance tuning (backpressure, vision/audio gating)

---

## 6 · Task Card Template

```yaml
id: "<A‑2>"
title: "Inject ViewModels with Hilt"
description: |
  Replace manual `viewModel()` factories in MainActivity with Hilt‑provided instances.
acceptance_criteria:
  - MainActivity annotated with @AndroidEntryPoint
  - ChatViewModel & DownloadViewModel annotated with @HiltViewModel and receive dependencies via constructor @Inject
  - Manual factories removed
estimate: "1 d"
```

---

## 7 · Next Steps

1. Create GitHub issues for **A‑1…A‑3**.
2. Branch naming: `feat/<id>-<slug>`.
3. Merge early, merge often—each milestone must remain installable.

Ship it fast 🚀

