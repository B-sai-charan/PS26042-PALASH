# M4 -> M6 Classroom Communication Behavior Handoff Document

This document defines the finalized classroom communication contract, local network behavior, message validation, deduplication rules, and UI state contract for M6 alignment.

---

## 1. Teacher & Student Configuration Contract

### Teacher Configuration (Source of Truth for Broadcasts)
- **Grade**: Selected Grade number (`1`, `2`, `3`). **PRIMARY ROUTING KEY**.
- **Section**: Optional Section string (`"A"`, `"B"`, or blank/null). **OPTIONAL SECONDARY FILTER**.
- **Subject**: Selected Subject string (`"Mathematics"`, `"Language"`).

### Student Configuration (Target Filter)
- **Grade**: Assigned Grade number (`1`, `2`, `3`). **PRIMARY ROUTING KEY**.
- **Section**: Optional Section string (`"A"`, `"B"`, or blank/null). **OPTIONAL SECONDARY FILTER**.
- **Teacher IP**: Configured local IP of Teacher tablet (e.g. `"192.168.43.1"`).
- **Teacher Port**: Configured local TCP port of Teacher server (`8080`).

---

## 2. Matching & Acceptance Rule
The authoritative, single reusable location for classroom message evaluation is [`SectionMatcher.evaluateMessage(...)`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/classroom/SectionMatcher.kt).

### Logic
```kotlin
// PRIMARY MATCH (MANDATORY FOR MVP ACCEPTANCE)
student.grade == message.grade 

// OPTIONAL SECONDARY MATCH (ONLY IF BOTH SIDES SPECIFY A NON-BLANK SECTION)
IF student.section IS NOT BLANK AND message.section IS NOT BLANK:
    student.section.equals(message.section, ignoreCase = true)
ELSE:
    ACCEPT (Grade match is sufficient for MVP)
```

### Result Classification ([`ClassroomMessageResult`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/classroom/ClassroomMessageResult.kt))
1. **`ACCEPTED`**: Target Grade matches (and optional Section matches if both present), payload is valid, and `messageId` is new -> Student UI displays received Santali translation card.
2. **`WRONG_GRADE`**: Target `message.grade != student.grade` -> Ignored by student device.
3. **`WRONG_SECTION`**: Both sides explicitly set sections, but `message.section != student.section` -> Ignored by student device.
4. **`DUPLICATE`**: Received `messageId == lastMessageId` -> Ignored (prevents duplicate rendering and auto-replay).
5. **`INVALID_MESSAGE`**: Payload fails contract validation (e.g. blank Santali text or timestamp <= 0) -> Ignored.
6. **`NO_MESSAGE`**: HTTP 404 response from server -> Student remains in waiting state.

---

## 3. Student-Facing Classroom Translation Message Payload Contract
Model class: [`ClassroomTranslationMessage.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/classroom/ClassroomTranslationMessage.kt)

```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "CLASSROOM_TRANSLATION",
  "grade": 2,
  "section": "A",
  "subject": "Mathematics",
  "lessonId": 201,
  "timestamp": 1725628800000,
  "santaliText": "ᱢᱤᱫ",
  "audioId": "num_001_1",
  "hindiText": null
}
```

- **`hindiText`**: Internal teacher-side artifact. Optional in payload and **MUST NOT** be displayed on Student UI.
- **`section`**: Optional for MVP routing.
- **`audioId`**: May be `null` if no matching local audio asset exists. Binary WAV audio bytes are **never** transmitted over network.

---

## 4. Local Server & Student Client Specification

### Teacher HTTP Server ([`TeacherClassroomServer.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/classroom/TeacherClassroomServer.kt))
- **Actual Port**: **`8080`** (`DEFAULT_PORT = 8080`).
- **Endpoints**:
  - `GET /health` -> `HTTP 200` `{"status":"ok","service":"palash-classroom-server"}`
  - `GET /classroom/latest` -> `HTTP 200` JSON payload or `HTTP 404` `{"status":"no_message"}`

### Student HTTP Client ([`StudentClassroomClient.kt`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/classroom/StudentClassroomClient.kt))
- **Implementation Status**: **IMPLEMENTED / VERIFIED**.
- Polling mechanism executes `GET http://<teacherIp>:8080/classroom/latest` every 2 seconds while active.

---

## 5. M6 UI State Contract Mapping

### Student UI States
| State Key | Meaning | Implementation Status |
| :--- | :--- | :--- |
| **`NO_TEACHER_MESSAGE`** | Student configured, no broadcast published yet. | **IMPLEMENTED** |
| **`WAITING_FOR_TEACHER`** | Student connected to teacher server, listening. | **IMPLEMENTED** |
| **`MESSAGE_RECEIVED`** | Target Grade + Section translation card received. | **IMPLEMENTED** |
| **`AUDIO_AVAILABLE`** | Received message maps to a local bundled M3 audio WAV asset. Button: `🔊 Listen`. | **IMPLEMENTED** |
| **`AUDIO_UNAVAILABLE`** | Message received but no local matching audio exists (`audioId == null`). Badge: `Audio unavailable`. | **IMPLEMENTED** |
| **`TEACHER_UNAVAILABLE`** | Teacher server offline or IP unreachable. Status: `🔴 Server Offline`. | **IMPLEMENTED** |
| **`INVALID_MESSAGE`** | Malformed JSON or failed contract validation. | **IMPLEMENTED** |
| **`DUPLICATE_MESSAGE`** | Internal state for ignoring repeated `messageId`. | **IMPLEMENTED** |

### Teacher UI States
| State Key | Meaning | Implementation Status |
| :--- | :--- | :--- |
| **`CLASSROOM_READY`** | Classroom session active, local server bound to IPv4. | **IMPLEMENTED** |
| **`CLASSROOM_SERVER_RUNNING`** | `TeacherClassroomServer` listening on port `8080`. | **IMPLEMENTED** |
| **`PROCESSING`** | Recording in progress or Vosk ASR / M1 translation active. | **IMPLEMENTED** |
| **`MESSAGE_READY`** | Translation generated and validated. | **IMPLEMENTED** |
| **`MESSAGE_SENT`** | Broadcast published to `/classroom/latest`. | **IMPLEMENTED** |
| **`NO_AUDIO_AVAILABLE`** | Audio asset missing for generated phrase (`audioId = null`). | **IMPLEMENTED** |
| **`STUDENT_NOT_REACHABLE`** | Passive HTTP server broadcast architecture (students pull via HTTP GET). | **NOT APPLICABLE** (Server is passive HTTP host) |

---

## 6. Audio Asset Provenance & Resolution
- **Resolution Flow**: `message.audioId` -> [`SantaliAudioRepository`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/audio/SantaliAudioRepository.kt) lookup -> asset path -> [`AudioPlaybackEngine`](file:///c:/Users/saich/Downloads/SIH/M4/app/src/main/java/com/sih2026/palash/audio/AudioPlaybackEngine.kt) playback.
- **Audio Specs**: 22050 Hz, mono, 16-bit PCM WAV.
- **Generation Method**: `pyttsx3-generated`.
- **Validation Status**: `UNVERIFIED` (Do not present as human-validated native Santali speech).

---

## 7. Physical Test Evidence & Disclosure
- **Teacher Device (Device A)**: Lenovo Tab K10 (`Lenovo_TB_X6C6F`, Android 11, ADB Serial: `HNP097HQ`, IP: `172.20.51.220`).
- **Student Device (Device B)**: iQOO Neo 9 Pro (`I2304`, Android 14, ADB Serial: `10BE830QG8000MX`, IP: `172.20.57.37`).
- **Physical Network**: Local Wi-Fi subnet (`172.20.0.0/16`), Teacher server port `8080`.
- **Physical Health Test**: `PASS` (`GET http://172.20.51.220:8080/health` → `HTTP 200 OK`, `status = ok`).
- **Physical Grade 2 → Grade 2 Routing**: `PASS` (Real payload received live on iQOO screen with grade=2, Santali Ol Chiki text "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟY ᱢᱮ।", Hindi text hidden).
- **Physical Wrong-Grade Filter (Grade 2 → Grade 1)**: `PASS` (`SectionMatcher` rejected Grade 2 message when student configured as Grade 1).
- **Physical Audio Playback**: `PASS_WITH_LIMITATIONS` (Offline asset `audio_1.wav` played; audio provenance is `UNVERIFIED` pyttsx3 demo audio).
- **Full Validation Report**: Documented in [`M4_M6_PHYSICAL_TWO_DEVICE_TEST_REPORT.md`](file:///c:/Users/saich/Downloads/SIH/M4/M4_M6_PHYSICAL_TWO_DEVICE_TEST_REPORT.md).

---

## 8. Known Limitations
1. **Audio Provenance**: Demo audio files (`audio_1.wav`) are pyttsx3/synthetic demo audio and MUST NOT be claimed as native-speaker validated Santali audio.
2. **Latency SLA**: End-to-end latency ≤3s SLA is not guaranteed across arbitrary long speech inputs.
3. **Translation Backend**: M1 translation engine requires local server / backend API for arbitrary phrase translation; bundled M3 audio fallback applies for Grade 2 Counting numbers.
