# M4 → M5 Student UI Wording Handoff

## 1. Scope

This handoff document provides the complete, authoritative inventory of all current and planned student-facing user interface (UI) strings in the PALASH Android codebase (`com.sih2026.palash`).

The objective is to enable M5 (Vernacular Content & Localization Team) to prepare exact Santali / Ol Chiki UI translations for Student Mode.

### Important Category Distinctions

To ensure clarity, the project maintains strict boundaries between four distinct asset categories:

1. **Application UI Strings**: Fixed Jetpack Compose & Android resource text guiding navigation, roles, configuration, and controls. (Scope of this handoff).
2. **M5 Educational Content**: Structured lesson sections, worksheet questions, and flashcard items stored in asset JSON files (`lesson_g2_counting.json`, `worksheet_g2_counting.json`, `flashcards_g2_counting.json`).
3. **M5 Santali Educational Content**: Pre-translated Ol Chiki text inside M5 Grade 2 Counting JSON payloads.
4. **M3 Audio Assets**: Bundled WAV audio files in `assets/audio/` (`num_001_1.wav` through `num_007_7.wav`).

> [!WARNING]
> M4 has NOT translated or validated any Santali UI wording in this baseline. Existing English strings have been preserved, and Ol Chiki labels currently present in code are draft UI placeholders from M6. All student-facing UI wording requires M5 native Santali review.

---

## 2. Student-Facing Source Files

Below is the list of Android source files containing current or planned student-facing text:

| Relative Path | Filename | Purpose / Why M5 Needs It | String Types Contained |
| :--- | :--- | :--- | :--- |
| `app/src/main/res/values/strings.xml` | `strings.xml` | Primary Android string resources | `STUDENT_SETUP`, `STUDENT_CLASSROOM`, `SHARED_UI`, `TEACHER_UI` |
| `app/src/main/java/.../ui/screens/student/StudentHomeScreen.kt` | `StudentHomeScreen.kt` | Student main live broadcast receiving screen | `STUDENT_CLASSROOM`, `STUDENT_UI` |
| `app/src/main/java/.../ui/screens/role/StudentSetupScreen.kt` | `StudentSetupScreen.kt` | Student device Grade & Section configuration screen | `STUDENT_SETUP`, `STUDENT_UI` |
| `app/src/main/java/.../ui/screens/role/RoleSelectionScreen.kt` | `RoleSelectionScreen.kt` | App launch role selection screen (Student vs Teacher) | `SHARED_UI`, `STUDENT_UI` |
| `app/src/main/java/.../ui/components/M6Components.kt` | `M6Components.kt` | Shared brand headers, grade cards, role cards | `SHARED_UI`, `STUDENT_UI` |
| `app/src/main/java/.../ui/screens/grade/GradeSelectionScreen.kt` | `GradeSelectionScreen.kt` | Grade 1 / 2 / 3 selection cards (M5 requested `GradeScreen.kt`) | `SHARED_UI` |
| `app/src/main/java/.../ui/screens/subject/SubjectTopicScreen.kt` | `SubjectTopicScreen.kt` | Subject & Topic selection UI (M5 requested `SubjectScreen.kt` & `TopicScreen.kt`) | `SHARED_UI` |
| `app/src/main/java/.../ui/screens/lesson/LessonScreen.kt` | `LessonScreen.kt` | Native Compose M5 lesson section renderer | `STUDENT_LEARNING_CONTENT_UI` |
| `app/src/main/java/.../ui/screens/worksheet/WorksheetScreen.kt` | `WorksheetScreen.kt` | Native Compose M5 worksheet question renderer | `STUDENT_LEARNING_CONTENT_UI` |
| `app/src/main/java/.../ui/screens/flashcard/FlashcardScreen.kt` | `FlashcardScreen.kt` | Native Compose M5 interactive flashcard viewer | `STUDENT_LEARNING_CONTENT_UI` |
| `app/src/main/java/.../ui/screens/pack/LanguagePackScreen.kt` | `LanguagePackScreen.kt` | Audio pack test & asset verification UI | `SHARED_UI`, `AUDIO_UI` |
| `app/src/main/java/.../ui/screens/result/TranslationResultScreen.kt` | `TranslationResultScreen.kt` | Translation verification UI (Teacher-initiated) | `TEACHER_UI`, `SHARED_UI` |
| `app/src/main/java/.../ui/navigation/NavGraph.kt` | `NavGraph.kt` | App routing structure (M5 requested `Navigation.kt`) | `TECHNICAL_INTERNAL` |
| `app/src/main/java/.../ui/localization/PalashStrings.kt` | `PalashStrings.kt` | Runtime Devanagari locale switcher utility | `TECHNICAL_INTERNAL` |

---

## 3. Student UI String Inventory

Total Student-Facing / Shared Strings Audited: **38 Strings**

---

## 4. Student Setup Strings

Strings visible during Student Mode initial setup (`StudentSetupScreen.kt` & `strings.xml`):

| ID | Current English/Hindi Text | Category | Student-Facing? | Needs Santali/Ol Chiki? | Notes / Context |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `SET_01` | Student Device Setup | `STUDENT_SETUP` | YES | YES | Setup screen title |
| `SET_02` | Configure Grade & Section for receiving classroom translations. | `STUDENT_SETUP` | YES | YES | Setup screen subtitle |
| `SET_03` | Select Grade (ᱪᱟᱱᱟᱪ ᱪᱷᱟᱸᱴᱟᱭ ᱢե) | `STUDENT_SETUP` | YES | YES | Grade section label (has draft Ol Chiki) |
| `SET_04` | Grade 1 | `STUDENT_SETUP` | YES | YES | Grade 1 chip / card |
| `SET_05` | Grade 2 (MVP Active) | `STUDENT_SETUP` | YES | YES | Grade 2 chip / card |
| `SET_06` | Select Section | `STUDENT_SETUP` | YES | YES | Section selection header |
| `SET_07` | Section A / Section B | `STUDENT_SETUP` | YES | YES | Section filter chips |
| `SET_08` | Teacher Local Network Address | `STUDENT_SETUP` | YES | YES | Network address section header |
| `SET_09` | Teacher IP | `STUDENT_SETUP` | YES | YES | IP input label |
| `SET_10` | Port | `STUDENT_SETUP` | YES | YES | Port input label |
| `SET_11` | Save & Launch Student View | `STUDENT_SETUP` | YES | YES | Main setup submit button |
| `SET_12` | Back to Role Selection | `STUDENT_SETUP` | YES | YES | Setup back button |

---

## 5. Student Classroom Strings

Strings visible on the Student live broadcast view (`StudentHomeScreen.kt`):

| ID | Current English/Hindi Text | Category | Student-Facing? | Needs Santali/Ol Chiki? | Notes / Context |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CLS_01` | ᱪᱮᱛᱮᱫᱤᱭᱟᱹ (Student Mode) | `STUDENT_CLASSROOM` | YES | YES | Student top header subtitle |
| `CLS_02` | Assigned Device Configuration | `STUDENT_CLASSROOM` | YES | YES | Configuration card header |
| `CLS_03` | Grade %1$d (ᱪᱟᱱᱟᱪ %2$s) — Section %3$s | `STUDENT_CLASSROOM` | YES | YES | Assigned grade/section badge |
| `CLS_04` | 🟢 Connected | `STUDENT_CLASSROOM` | YES | YES | Active network connection status |
| `CLS_05` | 🟡 Connecting | `STUDENT_CLASSROOM` | YES | YES | Connecting network status |
| `CLS_06` | 🟠 Section Mismatch | `STUDENT_CLASSROOM` | YES | YES | Ignored section status |
| `CLS_07` | 🔴 Server Offline | `STUDENT_CLASSROOM` | YES | YES | Unreachable teacher server status |
| `CLS_08` | 📢 TEACHER LESSON | `STUDENT_CLASSROOM` | YES | YES | Received broadcast card header |
| `CLS_09` | ᱥᱟᱱᱛᱟᱲᱤ (SANTALI) | `STUDENT_CLASSROOM` | YES | YES | Santali text card header |
| `CLS_10` | 🔊 Listen (ᱥᱟᱲᱮ ᱟᱸᱡᱚᱢ ᱢե) | `STUDENT_CLASSROOM` | YES | YES | Play audio button label |
| `CLS_11` | 🔊 Playing Santali Audio... | `STUDENT_CLASSROOM` | YES | YES | Audio playing state label |
| `CLS_12` | Audio unavailable | `STUDENT_CLASSROOM` | YES | YES | Missing audio fallback badge |
| `CLS_13` | 📡 Listening for Teacher Broadcast | `STUDENT_CLASSROOM` | YES | YES | Waiting state title |
| `CLS_14` | When the teacher speaks in Grade %1$d..., real-time Santali lessons will appear here... | `STUDENT_CLASSROOM` | YES | YES | Waiting state body text |
| `CLS_15` | ⚙️ Reconfigure Device Role / Teacher IP | `STUDENT_CLASSROOM` | YES | YES | Reconfigure button label |

---

## 6. Student Learning-Content UI Strings

Strings in M5 native learning screens (`LessonScreen.kt`, `WorksheetScreen.kt`, `FlashcardScreen.kt`):

| ID | Current English/Hindi Text | Category | Student-Facing? | Needs Santali/Ol Chiki? | Notes / Context |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `LRN_01` | कार्यपत्रक खोलें (Worksheet) | `STUDENT_LEARNING` | YES | YES | Open worksheet button |
| `LRN_02` | Practice Flashcards | `STUDENT_LEARNING` | YES | YES | Open flashcards button |
| `LRN_03` | Question %1$d of %2$d | `STUDENT_LEARNING` | YES | YES | Worksheet question progress indicator |
| `LRN_04` | सही उत्तर चुनें (Select Option): | `STUDENT_LEARNING` | YES | YES | Multiple choice question prompt |
| `LRN_05` | ✓ Correct Answer! | `STUDENT_LEARNING` | YES | YES | Correct feedback badge |
| `LRN_06` | ✕ Try Again | `STUDENT_LEARNING` | YES | YES | Incorrect feedback badge |
| `LRN_07` | Card %1$d of %2$d | `STUDENT_LEARNING` | YES | YES | Flashcard progress indicator |
| `LRN_08` | Tap card to flip | `STUDENT_LEARNING` | YES | YES | Flashcard interaction hint |
| `LRN_09` | Previous / Next | `STUDENT_LEARNING` | YES | YES | Pagination buttons |
| `LRN_10` | ⚠️ Unverified content | `SHARED_UI` | YES | YES | Content provenance warning badge |

---

## 7. Audio-Related Student Strings

Strings associated with audio controls in Student Mode:

| ID | Current English/Hindi Text | Category | Student-Facing? | Needs Santali/Ol Chiki? | Notes / Context |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `AUD_01` | 🔊 Listen (ᱥᱟᱲᱮ ᱟᱸᱡᱚᱢ ᱢե) | `STUDENT_CLASSROOM` | YES | YES | Classroom audio play button |
| `AUD_02` | 🔊 Playing Santali Audio... | `STUDENT_CLASSROOM` | YES | YES | Classroom audio playing state |
| `AUD_03` | Audio unavailable | `STUDENT_CLASSROOM` | YES | YES | Missing audio asset indicator |
| `AUD_04` | Unverified audio asset | `SHARED_UI` | YES | YES | M3 audio provenance warning |

---

## 8. Shared Components Containing Student-Facing Text

Shared components (`RoleSelectionScreen.kt`, `M6Components.kt`, `PalashBrandHeader`):

| ID | Current Text | Component | Student-Facing? | Needs Santali/Ol Chiki? |
| :--- | :--- | :--- | :--- | :--- |
| `SHR_01` | 🌺 पलाश / ᱯᱚᱞᱟᱥ | `PalashBrandHeader` | YES | YES (Has draft Ol Chiki) |
| `SHR_02` | भाषाओं को जोड़ें, सीखने का उजाला फैलाएं | `PalashBrandHeader` | YES | YES |
| `SHR_03` | 📡 ऑफ़लाइन काम करता है | `PalashBrandHeader` | YES | YES |
| `SHR_04` | विद्यार्थी / ᱪᱮᱛᱮᱫᱤᱭᱟᱹ | `M6RoleCard` | YES | YES |
| `SHR_05` | शिक्षक / ᱢᱟᱪᱮᱛ | `M6RoleCard` | YES | YES |
| `SHR_06` | PALASH — offline vernacular classroom companion | `RoleSelectionScreen` | YES | YES |

---

## 9. Technical / Internal Strings Excluded

The following items were audited and intentionally **EXCLUDED** from the Santali wording inventory because they are developer-facing, internal system logs, or network protocol constants:

- `TeacherClassroomServer` logs & HTTP status codes (`HTTP 200 OK`, `HTTP 404 No Message`).
- `VoskHindiAsrEngine` model paths & initialization logs (`model/hi`, `loading...`).
- `ClassroomMessageValidator` internal rejection strings (`"messageId must not be blank"`).
- `AppConfigManager` SharedPreferences keys (`"key_device_role"`, `"key_student_grade"`).
- Room Database table names and SQL queries (`translation_cache`, `m5_lessons`).
- Gradle task names, dependencies, and build properties.

---

## 10. Missing / Not-Yet-Implemented Student Screens

M5 requested strings for the following files which do not exist as separate screens in the current codebase:

1. **`AssessmentScreen.kt`**: Not created in UI yet. M5 assessment JSON schema (`assessments_g2_counting.json`) exists in the repository, but native Compose UI rendering is future scope.
2. **`GradeScreen.kt`**: Implemented as [`GradeSelectionScreen.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/ui/screens/grade/GradeSelectionScreen.kt).
3. **`SubjectScreen.kt` & `TopicScreen.kt`**: Combined into [`SubjectTopicScreen.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/ui/screens/subject/SubjectTopicScreen.kt).
4. **`Navigation.kt`**: Implemented as [`NavGraph.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/ui/navigation/NavGraph.kt).

---

## 11. Santali / Ol Chiki Wording Still Required

M5 needs to provide authoritative Santali (Ol Chiki) translations for:
1. All **Student Setup** titles, hints, labels, and button actions.
2. All **Student Classroom** status banners, broadcast labels, and network state indicators.
3. All **Student Learning Controls** (`Worksheet`, `Flashcards`, option prompts, correctness feedback).
4. All **Shared App Headers** and branding taglines.

---

## 12. Validation Warnings

- **Validation Status**: `UNVALIDATED`
- **Current Text State**: Baseline English UI with temporary M6 draft Ol Chiki headers.
- **Audio Assets**: Bundled M3 audio files (`num_001_1.wav`) are `pyttsx3-generated` and `UNVERIFIED`.
