# PALASH-NOVA — Bridging Languages, Building Learning

> **An offline-first bilingual educational assistant bridging Hindi and Santali (Ol Chiki) for primary school learners and educators.**

---

## 1. Problem Statement
In tribal and multilingual regions of India, primary school students whose mother tongue is **Santali** face steep linguistic barriers when instructional delivery is conducted predominantly in **Hindi**. Teachers often lack native Santali fluency, while students struggle with comprehension, leading to high disengagement and learning gaps. Reliable classroom internet connectivity is frequently unavailable in these regions, making cloud-dependent translation and speech services impractical.

---

## 2. Project Overview
**PALASH-NOVA** is an offline-capable Android application engineered to dismantle language barriers in primary education. It empowers:
- **Teachers**: Real-time Hindi speech recognition with instant translation into Santali (Ol Chiki script), alongside audio synthesis and verified curriculum phrase matching.
- **Students**: Self-paced, bilingual learning modules featuring interactive lessons, audio-supported flashcards, worksheets, and objective assessments aligned with primary school grades.

---

## 3. Verified Implemented Features

- **Role-Based Experience**:
  - **Teacher Mode**: Protected by a 4-digit PIN authentication flow, offering real-time classroom speech translation, grade selection, and doubts clarification.
  - **Student Mode**: Kid-friendly navigation with direct access to grade curricula without requiring authentication.
- **Curriculum-Aligned Learning (Grade 2 & Grade 3)**:
  - **Lessons**: Structured counting and foundational concepts with bilingual text and audio narration.
  - **Flashcards**: Interactive two-sided flashcards with flip animations and native audio playback.
  - **Worksheets**: Interactive exercises with immediate validation and audio guidance.
  - **Assessments**: Objective quizzes with score tracking and performance feedback.
- **Speech Recognition (ASR)**:
  - On-device, 100% offline Hindi automatic speech recognition powered by the **Vosk** Kaldi-based engine (`vosk-model-small-hi-0.22`).
- **Domain Translation Engine**:
  - Deterministic offline phrase matching against preloaded curriculum databases (`phrases.xlsx.generated.json`).
  - Network-aware repository: seamlessly falls back to offline curriculum matching when no backend server is configured.
- **Speech Synthesis (TTS)**:
  - Pre-recorded human Santali audio for core grade curriculum, numbers, questions, and flashcards.
  - Experimental on-device Piper neural TTS (`en_IN-spicor-medium.onnx`) executing via Microsoft ONNX Runtime Android with Ol Chiki / phonetic transliteration.

---

## 4. Student & Teacher Workflows

### Student Workflow
```
[Role Selection: Student]
       │
       ▼
[Grade Selection (Grade 2 / Grade 3)]
       │
       ▼
[Module Hub] ──┬──> [Lessons] (Bilingual reading & instruction audio)
               ├──> [Flashcards] (Interactive flip & audio pronunciation)
               ├──> [Worksheets] (Practice questions & instant validation)
               └──> [Assessments] (Progress quizzes & score report)
```

### Teacher Workflow
```
[Role Selection: Teacher]
       │
       ▼
[Security PIN Verification]
       │
       ▼
[Teacher Dashboard & Grade Selection]
       │
       ▼
[Voice Translation Screen]
       │
       ├─ [Record Hindi Instruction] ──> [Vosk Offline Hindi ASR]
       │                                         │
       ▼                                         ▼
[Translation Result Screen] <── [Curriculum Lookup / Transliteration Engine]
       │
       ├─ [View Ol Chiki & Hindi Script]
       ├─ [Play Santali Audio (Pre-recorded / Piper ONNX)]
       └─ [Replay Original Teacher Voice Recording]
```

---

## 5. Offline Capabilities & Operational Boundary

| Capability | Offline Status | Implementation Mechanism |
| :--- | :--- | :--- |
| **Hindi Speech-to-Text (ASR)** | **100% Offline** | Embedded Vosk model (`vosk-model-small-hi-0.22`) |
| **Curriculum Translation** | **100% Offline** | Embedded JSON dictionaries (`phrases.xlsx.generated.json`) |
| **Curriculum Learning Audio** | **100% Offline** | Bundled WAV assets under `assets/audio/santali/` |
| **Experimental Voice Synthesis** | **100% Offline** | Embedded Piper ONNX model with ONNX Runtime Android |
| **Open-Domain Translation** | **Requires Backend** | Connects to FastAPI / IndicTrans2 server when configured |

---

## 6. Technology Stack

Verified directly from repository build configuration (`build.gradle.kts`, `libs.versions.toml`):

- **Platform & Language**: Android (Kotlin 2.0.21, JVM Target 17)
- **Minimum SDK**: Android 9.0 (API Level 28)
- **Target / Compile SDK**: Android 15 (API Level 35)
- **Android Gradle Plugin**: 8.7.3
- **UI Framework**: Jetpack Compose BOM `2024.11.00` + Material 3
- **Navigation**: Jetpack Navigation Compose `2.8.4`
- **Local Persistence**: AndroidX Room `2.6.1` with Kotlin Symbol Processing (KSP `2.0.21-1.0.27`)
- **Networking**: Retrofit `2.11.0`, OkHttp `4.12.0`, Gson `2.11.0`
- **Speech Recognition (ASR)**: Alphacephei Vosk Android `0.3.47`
- **Neural Inference (TTS)**: Microsoft ONNX Runtime Android `1.18.0`
- **Testing**: JUnit 4.13.2, AndroidX Test Ext 1.2.1, Espresso 3.6.1

---

## 7. Architecture Overview

The application follows Modern Android Architecture (MVVM with unidirectional data flow):

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (RoleSelection, GradeSelection, VoiceScreen, Modules)│
└───────────────────────────┬────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────▼────────────────────────────┐
│                       ViewModels                       │
│ (VoiceTranslationViewModel, LessonViewModel, etc.)     │
└───────────────────────────┬────────────────────────────┘
                            │ Domain Calls
┌───────────────────────────▼────────────────────────────┐
│                    Repository Layer                    │
│   (TranslationRepository, ContentPackRepository)       │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
       ┌───────▼────────┐         ┌───────▼────────┐
       │ Local Engines  │         │ Remote Backend │
       │ - Vosk ASR     │         │ - FastAPI      │
       │ - Local Domain │         │ - IndicTrans2  │
       │ - Piper ONNX   │         │ (Optional URL) │
       │ - Room DB      │         └────────────────┘
       └────────────────┘
```

---

## 8. Project Directory Structure

```text
M4/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sih2026/palash/
│   │   │   │   ├── audio/          # Global audio player & recording helpers
│   │   │   │   ├── data/           # Room DB, entities, DAOs, repositories & network
│   │   │   │   ├── model/          # Domain data models & roles
│   │   │   │   ├── translation/    # Local domain offline translation engine
│   │   │   │   ├── tts/            # Piper ONNX inference & Ol Chiki transliteration
│   │   │   │   ├── ui/             # Jetpack Compose screens, theme & navigation
│   │   │   │   └── util/           # String utils, permissions & audio helpers
│   │   │   ├── assets/
│   │   │   │   ├── audio/santali/  # Bundled Santali curriculum audio recordings
│   │   │   │   ├── content/        # Grade 2 & Grade 3 lessons, flashcards, quizzes
│   │   │   │   ├── curriculum/     # Preloaded Hindi-Santali phrase dictionary
│   │   │   │   ├── models/piper/   # en_IN-spicor-medium.onnx & voice config
│   │   │   │   └── vosk-model-.../ # Offline Hindi Kaldi acoustic & language models
│   │   │   ├── res/                # Vector drawables, mipmaps & strings
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # 349 JVM unit tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── wrapper/                    # Gradle wrapper binaries
│   └── libs.versions.toml          # Version catalog
├── build.gradle.kts                # Top-level build configuration
├── settings.gradle.kts             # Settings script
├── .gitignore                      # Git exclusion rules
└── README.md                       # Repository documentation
```

---

## 9. Prerequisites & Setup

1. **Java Development Kit (JDK)**: JDK 17 or higher (tested with Oracle JDK 26 and OpenJDK 17).
2. **Android Studio**: Android Studio Ladybug (2024.2) or later recommended.
3. **Android SDK**:
   - SDK Platform 35 (Android 15)
   - SDK Build-Tools 35.0.0
   - Minimum supported device: Android 9.0 (API 28).

---

## 10. Build Instructions

### Assemble Debug APK
```bash
./gradlew assembleDebug
```
*Output artifact:* `app/build/outputs/apk/debug/app-debug.apk`

### Clean Build
```bash
./gradlew clean assembleDebug
```

---

## 11. Testing Instructions

### Run JVM Unit Tests
```bash
./gradlew testDebugUnitTest
```
*Current test suite baseline:* **349 tests passing, 0 failures, 0 errors, 0 skipped**.
*HTML Test Report:* `app/build/reports/tests/testDebugUnitTest/index.html`

### Run Instrumented Tests (Physical Device or Emulator required)
```bash
./gradlew connectedDebugAndroidTest
```

---

## 12. Required Runtime Models & Assets

The application bundles all production models within `app/src/main/assets/`:

1. **`models/piper/en_IN-spicor-medium.onnx`** (60.57 MB):
   Neural text-to-speech checkpoint loaded via ONNX Runtime Android.
2. **`vosk-model-small-hi-0.22/`** (~82 MB total):
   Kaldi Hindi speech recognition model package (`Gr.fst`, `HCLr.fst`, `final.mdl`, `final.ie`).
3. **`audio/santali/`** (4.8 MB):
   Verified human audio recordings for Grade 2 and Grade 3 numbers, worksheets, and flashcards.
4. **`curriculum/phrases.xlsx.generated.json`**:
   Precompiled authoritative classroom instruction translations.

---

## 13. Known Limitations

- **No Native Santali Neural TTS**: Santali is a low-resource language lacking a standalone pre-trained native Piper voice. Dynamic TTS uses an Indian English neural voice driven by Ol Chiki phonetic transliteration, alongside pre-recorded native human Santali audio for curriculum phrases.
- **Open-Domain Translation Scope**: Offline translation is deterministic and scoped to the preloaded educational curriculum database. Open-domain spontaneous speech translation outside the curriculum requires network access to the optional backend service.
- **Optical Character Recognition (OCR)**: OCR-based camera translation is not implemented in this application.

---

## 14. Future Scope

- Integration of a dedicated end-to-end Santali neural acoustic model as training datasets mature.
- Expansion of curriculum content to Grade 4 and Grade 5 subjects (Environmental Studies, Mathematics, General Science).
- Peer-to-peer Wi-Fi Direct synchronization between Teacher and Student devices for classroom quiz distribution without internet access.

---

## 15. License Status
*No license has been specified yet.*
All rights are reserved by the project owners. Selection and assignment of an open-source or proprietary license will be finalized by the repository maintainer.
