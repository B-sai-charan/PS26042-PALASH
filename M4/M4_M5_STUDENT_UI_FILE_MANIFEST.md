# M4 → M5 Student UI File Manifest

This document manifests all source/resource files included in the M5 student UI handoff archive (`M4_M5_STUDENT_UI_SOURCE_FILES.zip`), as well as files intentionally excluded with justifications.

---

## 1. Included Files (15 Source Files)

The ZIP package contains the following 15 source files preserving their exact relative project structure under `c:/Users/saich/Downloads/SIH/M4`:

```
app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── sih2026/
│       │           └── palash/
│       │               ├── ui/
│       │               │   ├── components/
│       │               │   │   ├── M6Components.kt
│       │               │   │   └── OfflineStatusIndicator.kt
│       │               │   ├── localization/
│       │               │   │   └── PalashStrings.kt
│       │               │   ├── navigation/
│       │               │   │   └── NavGraph.kt
│       │               │   └── screens/
│       │               │       ├── flashcard/
│       │               │       │   └── FlashcardScreen.kt
│       │               │       ├── grade/
│       │               │       │   └── GradeSelectionScreen.kt
│       │               │       ├── home/
│       │               │       │   └── HomeScreen.kt
│       │               │       ├── lesson/
│       │               │       │   └── LessonScreen.kt
│       │               │       ├── pack/
│       │               │       │   └── LanguagePackScreen.kt
│       │               │       ├── result/
│       │               │       │   └── TranslationResultScreen.kt
│       │               │       ├── role/
│       │               │       │   ├── RoleSelectionScreen.kt
│       │               │       │   ├── StudentSetupScreen.kt
│       │               │       │   └── TeacherPinScreen.kt
│       │               │       ├── student/
│       │               │       │   └── StudentHomeScreen.kt
│       │               │       ├── subject/
│       │               │       │   └── SubjectTopicScreen.kt
│       │               │       └── worksheet/
│       │               │           └── WorksheetScreen.kt
│       └── res/
│           └── values/
│               └── strings.xml
```

---

## 2. Intentionally Excluded Items & Justification

| Category / Directory | Reason for Exclusion |
| :--- | :--- |
| `build/` & `app/build/` | Gradle build outputs, compiled DEX, APKs, temporary object files. |
| `.gradle/` & `.idea/` | IDE metadata, Gradle caches, user settings. |
| `app/src/main/assets/vosk-model-small-hi-0.22/` | Large binary Vosk ASR model files (over 50 MB binary). |
| `app/src/main/assets/audio/` | Binary M3 WAV audio assets (`num_001_1.wav`). (Not UI source text). |
| `app/src/main/assets/m5/` | Content JSON files (`lesson_g2_counting.json`). (Managed separately by M5). |
| `PALASH_M5_FINAL_HANDOVER.zip` & `M6.zip` | Archive handoff inputs. |
| `app/src/main/java/.../asr/` | Speech recognition engine wrappers (Vosk). |
| `app/src/main/java/.../classroom/` | Teacher HTTP server (`TeacherClassroomServer.kt`) & server networking logic. |
| `app/src/main/java/.../data/local/db/` | Room database entities and DAOs. |
| `app/src/main/java/.../ui/screens/voice/` | Teacher-facing `VoiceTranslationScreen.kt` & ViewModel. |
| Backend / Python / Scratch files | Unrelated server reference scripts. |

---

## 3. Summary Statistics

- **Total Files Included in ZIP**: 15 files
- **Total Files Excluded**: All build directories, binary models, backend scripts, and teacher-only processing engines.
- **Source Paths Preserved**: YES (`app/src/main/...`)
