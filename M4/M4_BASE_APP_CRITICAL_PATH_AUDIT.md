# PALASH SIH 2026 — Base Application Critical Path Audit Report

**Audit Date:** 2026-09-06  
**Project:** PALASH Android App (`com.sih2026.palash`)  
**Package Directory:** `c:\Users\saich\Downloads\SIH\M4`  
**Build Status:** `BUILD SUCCESSFUL` (`.\gradlew.bat testDebugUnitTest assembleDebug`)  
**Unit Tests:** PASS (All unit tests passing)  

---

## 1. Current Application Navigation Map

```
[PalashNavHost] (startDestination: ROLE_SELECTION or saved HOME/STUDENT_HOME)
  │
  ├──► RoleSelectionScreen (route: "role_selection")
  │     ├── Select Teacher ──► TeacherPinScreen (route: "teacher_pin")
  │     │                       └── PIN Verified (1234) ──► HomeScreen (route: "home") [pops ROLE_SELECTION]
  │     └── Select Student ──► StudentSetupScreen (route: "student_setup")
  │                             └── Save Config ──► StudentHomeScreen (route: "student_home") [pops ROLE_SELECTION]
  │
  ├──► HomeScreen (route: "home") [TEACHER DASHBOARD]
  │     ├── Select Grade 1 / Grade 2 Card ──► GradeSelectionScreen (route: "grade_selection") [DUPLICATE]
  │     ├── "Start Lesson" Button ──────────► GradeSelectionScreen (route: "grade_selection") [DUPLICATE]
  │     └── "Manage Packs" Button ──────────► LanguagePackScreen (route: "language_pack")
  │
  ├──► GradeSelectionScreen (route: "grade_selection")
  │     └── Select Grade 1 / Grade 2 + Continue ──► SubjectTopicScreen (route: "subject_selection/{gradeName}")
  │
  ├──► SubjectTopicScreen (route: "subject_selection/{gradeName}")
  │     └── "Start Classroom" Button ──► VoiceTranslationScreen (route: "voice_translation/{gradeName}/{subjectName}/{topicName}")
  │
  ├──► LessonScreen (route: "lesson/{gradeName}/{subjectName}/{topicName}") [BYPASSED FROM MAIN FLOW]
  │     ├── "Open Worksheet" Button ──► WorksheetScreen (route: "worksheet") [NO PARAMETERS PASSED]
  │     └── "Back" Button ────────────► popBackStack()
  │
  ├──► VoiceTranslationScreen (route: "voice_translation/{gradeName}/{subjectName}/{topicName}")
  │     └── "View Result" / Publish ──► TranslationResultScreen (route: "translation_result?hindi=...&santaliNotice=...")
  │
  ├──► TranslationResultScreen (route: "translation_result?...")
  │     ├── "Open Worksheet" Button ──► WorksheetScreen (route: "worksheet") [NO PARAMETERS PASSED]
  │     └── "Back to Classroom" ──────► popBackStack(GRADE_SELECTION)
  │
  ├──► WorksheetScreen (route: "worksheet")
  │     ├── "Practice Flashcards" Button ──► FlashcardScreen (route: "flashcard") [NO PARAMETERS PASSED]
  │     └── "Back to Classroom" Button ────► popBackStack(GRADE_SELECTION)
  │
  └──► FlashcardScreen (route: "flashcard")
        └── "Back to Classroom" Button ────► popBackStack(GRADE_SELECTION)
```

---

## 2. Required Critical Path Navigation Map (Team Lead Guidance)

### Teacher Critical Path:
```
Role Selection
    ↓
Teacher PIN (1234)
    ↓
Grade Selection (Grade 1 / Grade 2)
    ↓
Subject & Topic Selection
    ↓
Lesson Screen (View M5 Lesson Content)
    ↓
Voice Translation (Speech ASR & Santali Broadcast)
    ↓
Translation Result
    ↓
Worksheet Page
    ↓
Flashcards
```

### Student Critical Path:
```
Role Selection
    ↓
Student Setup (Grade 1 / Grade 2 & Section)
    ↓
Content Selection (Browse Lessons / Worksheets / Flashcards)
    ↓
Lesson / Worksheet / Flashcard Viewers
```

---

## 3. Navigation Mismatch Analysis & QA Bug Root Causes

| QA Report / Area | Current Code Behavior | Root Cause in Source Code |
|---|---|---|
| **Duplicate Grade Selection** | Teacher selects Grade card on `HomeScreen`, but is taken to `GradeSelectionScreen` to pick Grade again. | In `HomeScreen.kt`, clicking Grade 1/2 cards calls `onStartLesson`, which navigates to `Routes.GRADE_SELECTION` instead of directly passing the selected grade to `Routes.buildSubjectRoute(grade)`. |
| **Bypassed Lesson Screen** | Tapping "Start Classroom" on `SubjectTopicScreen` skips `LessonScreen` entirely. | In `NavGraph.kt` (line 165), `onStartClassroom` navigates directly to `Routes.buildVoiceRoute(...)`, skipping `Routes.LESSON`. |
| **Hardcoded Grade 2 in Worksheets & Flashcards** | Grade 1 student/teacher opening Worksheet or Flashcards sees Grade 2 content. | `Routes.WORKSHEET` and `Routes.FLASHCARD` in `NavGraph.kt` take NO route arguments. `WorksheetViewModel` and `FlashcardViewModel` hardcode `loadGrade2Worksheet()` and `loadGrade2Flashcards()`. |
| **Missing Student Content Browser** | Student mode can only poll classroom Wi-Fi broadcasts; cannot browse offline lessons. | `StudentHomeScreen` has no buttons or tabs to navigate to local `LessonScreen`, `WorksheetScreen`, or `FlashcardScreen`. |
| **Inconsistent Back Navigation** | Back button on `WorksheetScreen` and `FlashcardScreen` returns to `GRADE_SELECTION`. | `NavGraph.kt` hardcodes `navController.popBackStack(Routes.GRADE_SELECTION, inclusive = false)` instead of popping to the calling screen. |

---

## 4. Startup & Session Persistence Audit

- **App Config Store:** `AppConfigManager` (`SharedPreferences` named `palash_app_config`).
- **First Launch (`isConfigured == false`):**
  - `NavGraph` resolves `initialDestination = Routes.ROLE_SELECTION`.
- **Re-opening App after configuration (`isConfigured == true`):**
  - If `deviceRole == DeviceRole.TEACHER`: Opens `Routes.HOME` (`HomeScreen`).
  - If `deviceRole == DeviceRole.STUDENT`: Opens `Routes.STUDENT_HOME` (`StudentHomeScreen`).
- **Audit Finding:** Persistent startup routing functions as designed. However, Teacher reopening the app lands on `HomeScreen`, which triggers the duplicate grade selection issue.

---

## 5. Grade Flow & Propagation Audit

- **Grade Catalog:**
  - `Grade 1` & `Grade 2` active in MVP UI.
  - `Grade 3` is **100% HIDDEN** from all user-facing screens (`DemoData.kt` `MVP_ALLOWED_GRADE_IDS` restricts catalog).
- **Grade Propagation:**
  - `GradeSelectionScreen` → passes `selectedGradeName` to `SubjectTopicScreen`.
  - `SubjectTopicScreen` → passes `gradeName` to `VoiceTranslationScreen`.
  - **Defect:** `TranslationResultScreen` -> `WorksheetScreen` -> `FlashcardScreen` loses the `gradeName` context and falls back to hardcoded Grade 2 in ViewModels.

---

## 6. Content Module Separation Audit

| Module | Current Route | View Model | Data Source / Asset | Behavior Classification |
|---|---|---|---|---|
| **Lesson** | `lesson/{gradeName}/{subjectName}/{topicName}` | `LessonViewModel` | `content/grade_2/lesson_g2_counting.json` | M5 6-section structured lesson card view. |
| **Worksheet** | `worksheet` | `WorksheetViewModel` | `content/grade_2/worksheet_g2_counting.json` | **Interactive 1-question-at-a-time Quiz** (Needs conversion to scrollable worksheet page). |
| **Flashcards** | `flashcard` | `FlashcardViewModel` | `content/grade_2/flashcards_g2_counting.json` | Interactive 2-sided flip cards (Front: Number/Hindi, Back: Ol Chiki). |
| **Assessment** | None (No UI route) | None | `content/grade_2/assessments_g2_counting.json` | Asset exists in assets folder, no UI screen attached. |

---

## 7. Detailed Worksheet Audit (Current vs Intended)

### Current Implementation (`WorksheetScreen.kt` & `WorksheetViewModel.kt`):
- Displays **one question at a time** using `val q = uiState.currentQuestion`.
- Has "Previous" and "Next" question navigation buttons.
- On option click, immediately evaluates answer correctness (`isCorrect = (option == q.correctAnswer)`) and shows green/red feedback.
- Operates like an **interactive quiz**, not a scrollable worksheet.

### Intended Requirement:
1. Vertically scrollable page displaying **ALL questions** simultaneously.
2. Students select options for each question; selections remain visible and editable.
3. Single **"Submit / Submit Worksheet"** button at the bottom of the scrollable page.
4. Summary evaluation and score displayed upon submission.

---

## 8. Detailed Flashcard Audit

### Current Implementation (`FlashcardScreen.kt` & `FlashcardViewModel.kt`):
- **Front Side (`isFlipped == false`):** Number display (`card.numberDisplay`), Visual prompt (`card.visual`), Hindi text (`card.hindi`), Math expression (`card.operand1 + card.operator + card.operand2`).
- **Back Side (`isFlipped == true`):** Santali Ol Chiki text (`card.santali`).
- **Card Flip:** Tapping card toggles flip state.
- **Card Navigation:** "Previous" and "Next" buttons advance through the card list.
- **Internal IDs & Badges:** `cardId` and validation badges are hidden from the student UI.
- **Defect:** **NO Audio Button / Audio Playback!** `FlashcardScreen` has no audio button, and `FlashcardViewModel` has no playback engine connection, even though `M5Flashcard` specifies audio references.

---

## 9. Audio Mapping & Provenance Truth Audit

- **Asset Path:** `app/src/main/assets/audio/santali/g2_counting/` (`numbers/`, `worksheet/`, `flashcards/`, `assessment/`).
- **Resolution Engine:** `SantaliAudioRepositoryImpl` indexes `.wav` files by `audioId`. `AndroidAudioPlaybackEngineImpl` plays assets using Android `MediaPlayer`.
- **Provenance Truth:** All M3/M5 audio assets are `pyttsx3-generated` synthetic demo assets (`validationStatus = UNVERIFIED`).
- **Screen Integration:**
  - `StudentHomeScreen`: Audio control present (`🔊 Listen`).
  - `VoiceTranslationScreen`: Audio status integrated.
  - `WorksheetScreen`: Audio control MISSING.
  - `FlashcardScreen`: Audio control MISSING.

---

## 10. Grade 1 vs Grade 2 Asset Availability

- **Grade 2 Content:**
  - `lesson_g2_counting.json` (Available in assets)
  - `worksheet_g2_counting.json` (Available in assets)
  - `flashcards_g2_counting.json` (Available in assets)
  - `assessments_g2_counting.json` (Available in assets)
  - 20+ WAV audio files under `audio/santali/g2_counting/` (Available in assets)
- **Grade 1 Content:**
  - Assets directory `app/src/main/assets/content/grade_1` **DOES NOT EXIST YET**.
  - Grade 1 UI cards exist in selection screens, but selecting Grade 1 currently falls back to Grade 2 content or unavailable status.

---

## 11. Offline Capability Matrix

| Feature | Execution Engine | Offline Status | Dependency |
|---|---|---|---|
| **Role & Grade Selection** | Local Jetpack Compose UI | **FULLY LOCAL / OFFLINE** | None |
| **Lesson Viewer** | Local asset loader + Room | **FULLY LOCAL / OFFLINE** | Local JSON asset |
| **Worksheet Viewer** | Local asset loader + Room | **FULLY LOCAL / OFFLINE** | Local JSON asset |
| **Flashcards Viewer** | Local asset loader + Room | **FULLY LOCAL / OFFLINE** | Local JSON asset |
| **Audio Playback** | `AndroidAudioPlaybackEngineImpl` | **FULLY LOCAL / OFFLINE** | Bundled WAV asset |
| **Vosk Hindi Speech ASR** | Local `VoskHindiAsrEngine` | **FULLY LOCAL / OFFLINE** | Assets Vosk model |
| **Cached Phrase Translation** | Room `translation_cache` | **CACHE-DEPENDENT** | Room seed database |
| **Arbitrary NMT Translation** | M1 Backend NMT API | **NETWORK-DEPENDENT** | M1 Backend (`/translate`) |
| **Classroom Broadcast** | Local HTTP Server (Port 8080) | **LOCAL WI-FI DEPENDENT** | Shared Wi-Fi (No Internet needed) |

---

## 12. Regression Analysis of Recent M6 Changes

- `PalashApplication.kt`: Added `TeacherClassroomServerHolder.server.start()`. (Safe background server start; no regression to base app).
- `NavGraph.kt`: `onStartClassroom` was changed to navigate to `buildVoiceRoute(...)`.
  - **Regression Identified:** Tapping "Start Classroom" in `SubjectTopicScreen` bypasses `LessonScreen`. `LessonScreen` is unreachable from `SubjectTopicScreen`.
- `VoiceTranslationScreen.kt`: Added sample publish button for physical testing. (Non-breaking for base app).

---

## 13. Prioritized Bug List (P0 / P1 / P2)

### P0 — Must Fix for Base Application MVP Demo
1. **BUG-P0-01:** Duplicate Grade Selection in Teacher Flow (`HomeScreen` -> `GradeSelectionScreen`).
2. **BUG-P0-02:** `SubjectTopicScreen` bypasses `LessonScreen` and skips straight to `VoiceTranslationScreen`.
3. **BUG-P0-03:** Grade parameter NOT passed to `WorksheetScreen` and `FlashcardScreen` (hardcodes Grade 2).
4. **BUG-P0-04:** Missing Student Content Selection View (`StudentHomeScreen` only polls Wi-Fi; cannot browse offline Lessons/Worksheets/Flashcards).
5. **BUG-P0-05:** Missing Audio Playback Control on `FlashcardScreen`.

### P1 — Important Polish & Flow Correctness
1. **BUG-P1-01:** `WorksheetScreen` operates as an item-by-item quiz instead of a vertically scrollable page with a Submit button.
2. **BUG-P1-02:** Missing Grade 1 content JSON assets in `assets/content/grade_1`.
3. **BUG-P1-03:** Back button on `WorksheetScreen` and `FlashcardScreen` pops to `GRADE_SELECTION` instead of current topic/lesson.

### P2 — Future Enhancements
1. **BUG-P2-01:** Automated mDNS/UDP teacher server discovery on local Wi-Fi.
2. **BUG-P2-02:** Dedicated Assessment UI module for `assessments_g2_counting.json`.
