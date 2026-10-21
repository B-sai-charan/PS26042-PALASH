# BUG-P0-02 — LESSON SCREEN FLOW FIX

**Date:** 2026-09-06  
**Project:** PALASH Android App (`com.sih2026.palash`)  
**Package Location:** `c:\Users\saich\Downloads\SIH\M4`  
**Build Status:** `BUILD SUCCESSFUL` (`.\gradlew.bat testDebugUnitTest assembleDebug`)  
**Unit Tests:** PASS (183/183 tests passing)  

---

## A. ROOT CAUSE

In `NavGraph.kt`, the `SubjectTopicScreen` composable destination's `onStartClassroom` callback was previously configured to navigate directly to `Routes.buildVoiceRoute(gradeName, subject, topic)`. 

This bypassed `LessonScreen` entirely, preventing teachers from viewing the M5 educational lesson content before entering the voice translation/classroom broadcast phase.

---

## B. FILES CHANGED

1. **[`NavGraph.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/ui/navigation/NavGraph.kt)**
   - Reconnected `SubjectTopicScreen`'s `onStartClassroom` callback to navigate to `Routes.buildLessonRoute(gradeName, subject, topic)`.
   - Wired `LessonScreen`'s `onStartClassroom` parameter to navigate to `Routes.buildVoiceRoute(gradeName, subjectName, topicName)`.

2. **[`LessonScreen.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/ui/screens/lesson/LessonScreen.kt)**
   - Added `onStartClassroom: () -> Unit = {}` parameter signature.
   - Re-ordered bottom action buttons to provide:
     - Primary Button: `teacherStringResource(R.string.subject_start_classroom)` -> triggers `onStartClassroom` (`VoiceTranslationScreen`).
     - Secondary Button: `"कार्यपत्रक खोलें (Worksheet)"` -> triggers `onOpenWorksheet` (`WorksheetScreen`).
     - Tertiary Button: `"वापस जाएं (Back)"` -> triggers `onBack` (`popBackStack()`).

3. **[`TeacherHomeNavigationUnitTest.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/test/java/com/sih2026/palash/ui/navigation/TeacherHomeNavigationUnitTest.kt)**
   - Added unit test cases verifying `SUBJECT_SELECTION` -> `LESSON` route structure, Grade 1/Grade 2 context preservation, and `VoiceTranslationScreen` reachability.

---

## C. FLOW BEFORE (BYPASSED LESSON)

```
Teacher Role
    ↓
Teacher PIN (1234)
    ↓
Home Screen
    ↓
Grade 1 / Grade 2 Card
    ↓
Subject & Topic Screen
    ↓  (Start Classroom)
VoiceTranslationScreen  [LESSON SCREEN BYPASSED!]
```

---

## D. FLOW AFTER (RESTORED CRITICAL PATH)

```
Teacher Role
    ↓
Teacher PIN (1234)
    ↓
Home Screen
    ↓
Grade 1 / Grade 2 Card
    ↓
Subject & Topic Screen
    ↓  (Start Classroom)
LessonScreen (View M5 Educational Lesson Content)
    ├─► Start Classroom (VoiceTranslationScreen)
    ├─► Open Worksheet (WorksheetScreen)
    └─► Back (Returns to Subject & Topic)
```

---

## E. GRADE CONTEXT PRESERVATION

- `SubjectTopicScreen` passes `gradeName` ("Grade 1" or "Grade 2") to `Routes.buildLessonRoute(gradeName, subject, topic)`.
- `NavGraph` parses `gradeName` from the route and injects it into `LessonScreen`.
- `LessonScreen` top badge renders:
  - `Grade 1 | Mathematics > Counting` for Grade 1.
  - `Grade 2 | Mathematics > Counting` for Grade 2.
- Grade 3 is strictly excluded from all user-facing catalog selections and route options.

---

## F. TEST RESULTS

- **Unit Tests:** `PASS` (183 tests executed, 0 failures)
  ```bash
  .\gradlew.bat testDebugUnitTest
  # BUILD SUCCESSFUL in 18s
  ```
- **Assembly Build:** `PASS`
  ```bash
  .\gradlew.bat assembleDebug
  # BUILD SUCCESSFUL in 14s
  ```
- **APK Path:** `app/build/outputs/apk/debug/app-debug.apk`
- **APK Size:** `114,494,862 bytes` (~109.19 MB)

---

## G. PHYSICAL DEVICE RESULTS (Lenovo Tab K10 — Serial: `HNP097HQ`)

Physical validation was executed on the Lenovo Tab K10:

- **Teacher PIN:** `PASS` (PIN `1234` accepted; opened Home).
- **Grade 2 Selection:** `PASS` (Selected Grade 2 -> `SubjectTopicScreen` displayed `कक्षा चुनें: Grade 2`).
- **LessonScreen Entry:** `PASS` (Tapped `कक्षा शुरू करें (Classroom)` -> `LessonScreen` opened displaying `Grade 2 | Mathematics > Counting` badge and M5 lesson sections).
- **Voice Translation Reachability:** `PASS` (Tapped `कक्षा शुरू करें` on `LessonScreen` -> `VoiceTranslationScreen` opened showing `कक्षा 2 | Mathematics > Counting`).
- **Back Navigation:** `PASS` (Back from `VoiceTranslationScreen` returned to `LessonScreen`; Back from `LessonScreen` returned to `SubjectTopicScreen`).
- **Grade 1 Flow:** `PASS` (Tapped Grade 1 on Home -> `SubjectTopicScreen` -> `LessonScreen` displayed `Grade 1 | Mathematics > Counting`).
- **Grade 3 Hidden:** `PASS` (Confirmed Grade 3 remains absent from all selection screens).

---

## H. REGRESSION RESULTS

- **Teacher PIN Auth:** Unchanged & verified.
- **Home Direct Card Navigation:** Unchanged & verified (BUG-P0-01 fix preserved).
- **Worksheet / Flashcard Navigation:** Unchanged & intact.
- **M1/M5 Data & Provenance:** Unchanged (`pyttsx3-generated` / `UNVERIFIED` truth preserved).
- **Two-Device Networking Code:** Unchanged & frozen.

---

## I. SOURCE_CHANGES

- `app/src/main/java/com/sih2026/palash/ui/navigation/NavGraph.kt`
- `app/src/main/java/com/sih2026/palash/ui/screens/lesson/LessonScreen.kt`
- `app/src/test/java/com/sih2026/palash/ui/navigation/TeacherHomeNavigationUnitTest.kt`
