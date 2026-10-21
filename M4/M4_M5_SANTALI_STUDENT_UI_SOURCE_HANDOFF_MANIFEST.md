# M4 → M5 Santali Student UI Source Handoff Manifest

## Purpose

This source handoff package (`M4_M5_SANTALI_STUDENT_UI_SOURCE_HANDOFF.zip`) provides the complete current Android source code, UI models, classroom communication contracts, audio control utilities, M5 Grade 2 content context, and existing wording inventories required for M5 (Vernacular Content & Localization Team) to prepare authoritative Student-facing Santali / Ol Chiki UI wording.

M5 is responsible for wording preparation. M4 provides the current Android codebase context and structure.

---

## Included Files (37 Files)

| # | Relative Path | Category | Why M5 Needs It |
|---|---|---|---|
| 1 | `app/src/main/res/values/strings.xml` | `LOCALIZATION` | Primary Android string resource XML file containing baseline setup and UI strings. |
| 2 | `app/src/main/java/com/sih2026/palash/ui/localization/PalashStrings.kt` | `LOCALIZATION` | Runtime Devanagari locale configuration resolver utility. |
| 3 | `app/src/main/java/com/sih2026/palash/ui/screens/role/RoleSelectionScreen.kt` | `SHARED_UI` | App launch role selection screen containing Student role entry wording (`विद्यार्थी` / `ᱪᱮᱛᱮᱫᱤᱭᱟᱹ`). |
| 4 | `app/src/main/java/com/sih2026/palash/ui/screens/role/StudentSetupScreen.kt` | `STUDENT_SETUP` | Student device Grade & Section setup screen containing setup titles, labels, and action buttons. |
| 5 | `app/src/main/java/com/sih2026/palash/ui/screens/student/StudentHomeScreen.kt` | `STUDENT_CLASSROOM` | Main Student live broadcast receiving UI containing status banners, translation cards, and playback controls. |
| 6 | `app/src/main/java/com/sih2026/palash/ui/screens/grade/GradeSelectionScreen.kt` | `LEARNING_CONTENT` | Grade 1 / 2 / 3 selection cards UI (M5 requested `GradeScreen.kt`). |
| 7 | `app/src/main/java/com/sih2026/palash/ui/screens/subject/SubjectTopicScreen.kt` | `LEARNING_CONTENT` | Subject & Topic selection UI (M5 requested `SubjectScreen.kt` & `TopicScreen.kt`). |
| 8 | `app/src/main/java/com/sih2026/palash/ui/screens/lesson/LessonScreen.kt` | `LEARNING_CONTENT` | Native Compose M5 lesson section renderer displaying Hindi & Santali lesson sections. |
| 9 | `app/src/main/java/com/sih2026/palash/ui/screens/worksheet/WorksheetScreen.kt` | `LEARNING_CONTENT` | Native Compose M5 worksheet question renderer displaying question prompts and option buttons. |
| 10 | `app/src/main/java/com/sih2026/palash/ui/screens/flashcard/FlashcardScreen.kt` | `LEARNING_CONTENT` | Native Compose M5 interactive flashcard viewer displaying card flip hints and navigation controls. |
| 11 | `app/src/main/java/com/sih2026/palash/ui/screens/result/TranslationResultScreen.kt` | `SHARED_UI` | Translation verification UI displaying source Hindi and target Santali text views. |
| 12 | `app/src/main/java/com/sih2026/palash/ui/screens/pack/LanguagePackScreen.kt` | `STUDENT_AUDIO` | Language pack management UI containing audio asset test controls. |
| 13 | `app/src/main/java/com/sih2026/palash/ui/navigation/NavGraph.kt` | `NAVIGATION` | Application route definition and navigation graph (M5 requested `Navigation.kt`). |
| 14 | `app/src/main/java/com/sih2026/palash/ui/components/M6Components.kt` | `SHARED_UI` | Reusable brand header (`પલાશ` / `ᱯᱚᱞᱟᱥ`), role cards, grade cards, and unverified warning badges. |
| 15 | `app/src/main/java/com/sih2026/palash/ui/components/OfflineStatusIndicator.kt` | `SHARED_UI` | Offline network status badge component (`Online` / `Offline`). |
| 16 | `app/src/main/java/com/sih2026/palash/data/config/AppConfigManager.kt` | `STUDENT_SETUP` | Persistent configuration manager for Student device role, Grade, Section, Teacher IP, and Port. |
| 17 | `app/src/main/java/com/sih2026/palash/model/TeacherSession.kt` | `STUDENT_SETUP` | Data models `TeacherSession` and `StudentConfig`. |
| 18 | `app/src/main/java/com/sih2026/palash/model/DeviceRole.kt` | `STUDENT_SETUP` | Enum defining `TEACHER` and `STUDENT` operating modes. |
| 19 | `app/src/main/java/com/sih2026/palash/classroom/StudentClassroomClient.kt` | `STUDENT_CLASSROOM` | HTTP client polling Teacher server `/classroom/latest` for real-time broadcasts. |
| 20 | `app/src/main/java/com/sih2026/palash/classroom/ClassroomTranslationMessage.kt` | `STUDENT_CLASSROOM` | Strongly typed classroom broadcast message payload data contract. |
| 21 | `app/src/main/java/com/sih2026/palash/classroom/ClassroomMessageType.kt` | `STUDENT_CLASSROOM` | Message type enum (`CLASSROOM_TRANSLATION`). |
| 22 | `app/src/main/java/com/sih2026/palash/classroom/ClassroomMessageResult.kt` | `STUDENT_CLASSROOM` | Result classification enum (`ACCEPTED`, `WRONG_GRADE`, `WRONG_SECTION`, `DUPLICATE`, etc.). |
| 23 | `app/src/main/java/com/sih2026/palash/classroom/SectionMatcher.kt` | `STUDENT_CLASSROOM` | Primary Grade routing and optional Section filtering matching engine. |
| 24 | `app/src/main/java/com/sih2026/palash/audio/SantaliAudioRepository.kt` | `STUDENT_AUDIO` | Interface for resolving audio asset paths from `audioId`. |
| 25 | `app/src/main/java/com/sih2026/palash/audio/SantaliAudioRepositoryImpl.kt` | `STUDENT_AUDIO` | Asset repository managing local bundled WAV files (`num_001_1.wav`). |
| 26 | `app/src/main/java/com/sih2026/palash/audio/AudioPlaybackEngine.kt` | `STUDENT_AUDIO` | Interface for audio playback execution. |
| 27 | `app/src/main/java/com/sih2026/palash/audio/AndroidAudioPlaybackEngineImpl.kt` | `STUDENT_AUDIO` | Android `MediaPlayer` implementation for playing Santali audio assets. |
| 28 | `app/src/main/java/com/sih2026/palash/audio/AudioPlaybackState.kt` | `STUDENT_AUDIO` | Playback state enum (`IDLE`, `LOADING`, `PLAYING`, `ERROR`). |
| 29 | `app/src/main/java/com/sih2026/palash/data/model/m5/M5Models.kt` | `LEARNING_CONTENT` | M5 Grade 2 Kotlin data models (`M5LessonContent`, `M5Section`, `M5WorksheetContent`, `M5FlashcardContent`). |
| 30 | `app/src/main/java/com/sih2026/palash/data/repository/M5ContentRepository.kt` | `LEARNING_CONTENT` | Repository loading and parsing M5 Grade 2 JSON assets from Android assets. |
| 31 | `app/src/main/assets/content/grade_2/lesson_g2_counting.json` | `LEARNING_CONTENT` | Bundled M5 Grade 2 Counting lesson JSON asset (Context only). |
| 32 | `app/src/main/assets/content/grade_2/worksheet_g2_counting.json` | `LEARNING_CONTENT` | Bundled M5 Grade 2 Counting worksheet JSON asset (Context only). |
| 33 | `app/src/main/assets/content/grade_2/flashcards_g2_counting.json` | `LEARNING_CONTENT` | Bundled M5 Grade 2 Counting flashcards JSON asset (Context only). |
| 34 | `app/src/main/assets/content/grade_2/assessments_g2_counting.json` | `LEARNING_CONTENT` | Bundled M5 Grade 2 Counting assessments JSON asset (Context only). |
| 35 | `M4_M5_STUDENT_UI_WORDING_HANDOFF.md` | `WORDING_INVENTORY` | Detailed markdown handoff document defining scope, screen breakdown, and wording rules. |
| 36 | `M4_M5_STUDENT_UI_WORDING_INVENTORY.csv` | `WORDING_INVENTORY` | Machine-readable CSV inventory of all 38 audited student UI strings. |
| 37 | `M4_M5_STUDENT_UI_FILE_MANIFEST.md` | `WORDING_INVENTORY` | Initial file manifest document. |

---

## Excluded Files & Justification

| File / Directory | Reason for Exclusion |
| :--- | :--- |
| `TeacherPinScreen.kt` | Teacher-only PIN verification screen (`1234`). Unrelated to Student UI wording. |
| `VoiceTranslationScreen.kt` & `VoiceTranslationViewModel.kt` | Teacher-only ASR and voice translation control screen. |
| `TeacherClassroomServer.kt` | Teacher-side HTTP server listening on port `8080`. |
| `app/src/main/assets/vosk-model-small-hi-0.22/` | Large binary Hindi ASR model files (>50 MB). |
| `app/src/main/assets/audio/*.wav` | Binary WAV audio files (`num_001_1.wav`). (Wording context provided via JSON & code). |
| `build/` & `app/build/` | Compiled DEX, APKs, temporary object files. |
| `.gradle/` & `.idea/` | IDE metadata and Gradle caches. |
| Keystores, signing files, secrets | Security exclusions. |

---

## Validation Warning

> [!WARNING]
> M4 has not independently validated Santali/Ol Chiki UI wording. M5 must provide proposed wording and clearly flag terms requiring native/context validation.
>
> Existing M5 Grade 2 educational content retains its current validation/provenance status (`model_generated_unvalidated`) and must not be upgraded by M4.

---

## Source Integrity

Source status check:
```
SOURCE_CHANGES = NONE
```
No source code files were modified or altered during this packaging task.
