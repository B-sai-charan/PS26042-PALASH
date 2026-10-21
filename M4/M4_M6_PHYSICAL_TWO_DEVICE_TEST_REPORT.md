# M4/M6 Physical Two-Device Classroom Validation Report

**Test Date:** 2026-09-06  
**APK Build Version:** `1.0.0` (versionCode: `1`, Debug build)  
**APK Size:** `114,494,862` bytes (~109.19 MB)  
**Build Result:** SUCCESSFUL (`.\gradlew.bat testDebugUnitTest assembleDebug`)  

---

## 1. Physical Device Inventory & ADB Verification

| Device Role | Device Model | ADB Serial | Android OS / Hardware | Status |
|---|---|---|---|---|
| **Device A (TEACHER)** | Lenovo Tab K10 (`Lenovo_TB_X6C6F`) | `HNP097HQ` | Android 11 (ARM64) | Connected & Authorized (`device`) |
| **Device B (STUDENT)** | iQOO Neo 9 Pro (`I2304`) | `10BE830QG8000MX` | Android 14 (ARM64) | Connected & Authorized (`device`) |

*Same identical APK (`app-debug.apk`) installed on both devices.*

---

## 2. Network Topology & Discovery

- **Network Type:** Shared Local Wi-Fi Network (`172.20.0.0/16` Subnet)
- **Teacher Device IP (Lenovo Tab K10):** `172.20.51.220`
- **Student Device IP (iQOO Neo 9 Pro):** `172.20.57.37`
- **Internet Available:** YES (Ping `8.8.8.8` 0% packet loss)
- **Teacher Server Local TCP Port:** `8080`
- **Device-to-Device Network Reachability:** PASS (ICMP ping 0% packet loss, RTT avg 66.2ms)

---

## 3. Teacher Server & Health Endpoint Verification

- **Teacher Navigation:** `Teacher` → `PIN (1234)` → `Grade 2` → `Mathematics` → `Counting` → `Start Classroom` (Opens `VoiceTranslationScreen`).
- **Server Initialization:** `TeacherClassroomServer` started on `172.20.51.220:8080`.
- **Student Device Health Probe:** Tested from `10BE830QG8000MX` via HTTP `GET http://172.20.51.220:8080/health`.

### Response Payload:
```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Content-Length: 51
Connection: close

{"status":"ok","service":"palash-classroom-server"}
```
- **Health Test Result:** `PASS` (`HTTP 200 OK`, `status = ok`).

---

## 4. Physical Two-Device Test Execution Matrix

| Test ID | Test Description | Target Requirement | Real Physical Result | Status |
|---|---|---|---|---|
| **TEST-01** | **Grade 2 → Grade 2 Positive Routing** | Student receives broadcast from Lenovo over local Wi-Fi | Payload received live on iQOO screen with grade=2 and Santali/Ol Chiki text. | **PASS** |
| **TEST-02** | **Student Local Audio Playback** | Audio plays locally without internet / download | Local asset `audio_1.wav` plays on iQOO. Audio provenance is UNVERIFIED demo audio. | **PASS_WITH_LIMITATIONS** |
| **TEST-03** | **Grade 2 → Grade 1 Wrong-Grade Routing** | Grade 1 student must NOT display Grade 2 messages | `SectionMatcher.matchesGrade(2, 1)` evaluated `false`; message rejected cleanly on student. | **PASS** |
| **TEST-04** | **Duplicate Message Protection** | Same `messageId` not repeatedly displayed | Polling loop ignored duplicate `messageId` without UI duplication. | **PASS** |
| **TEST-05** | **Hindi Privacy Protection** | Hindi transcript hidden on student device | Student UI renders only `santaliText` ("ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।"); `hindiText` is NOT displayed. | **PASS** |
| **TEST-06** | **Teacher Grade Switching** | Grade switch Grade 2 → Grade 1 → Grade 2 without restart | Active teacher grade state changes cleanly without stale Grade 1 routing leftover. | **PASS** |
| **TEST-07** | **Offline Learning Content** | Lessons, worksheets, flashcards, local audio work offline | Local M5 curriculum assets load from Room/assets offline without internet connection. | **PASS_WITH_LIMITATIONS** |
| **TEST-08** | **End-to-End Classroom Workflow** | Teacher input → ASR → Translation → Broadcast → Student playback | Full voice/text pipeline executed live across physical devices. | **PASS_WITH_LIMITATIONS** |
| **TEST-09** | **Unit Test Suite Regression** | Unit test suite passing baseline | `.\gradlew.bat testDebugUnitTest` executed cleanly; all unit tests passed. | **PASS** |

---

## 5. Live Physical Payload Evidence Excerpt

Retrieved live from Lenovo (`172.20.51.220:8080`) by iQOO student device via HTTP `GET /classroom/latest`:

```json
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Content-Length: 347
Connection: close

{
  "audioId": "audio_1",
  "grade": 2,
  "hindiText": "नमस्ते मर्द जो आज हम गिनती सीखेंगे",
  "messageId": "8921eb7b-a18b-40c4-9fff-414d92a0029f",
  "santaliText": "ᱢᱚᱬᱮ ᱩﾙ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।",
  "section": "A",
  "subject": "Mathematics",
  "timestamp": 1788710792028,
  "type": "CLASSROOM_TRANSLATION"
}
```

---

## 6. Physical Validation Limitations & Truth Declarations

1. **Audio Provenance:** Demo audio files (`audio_1.wav`) are pyttsx3/synthetic demo audio and MUST NOT be claimed as native-speaker validated Santali audio.
2. **End-to-End Latency:** While text broadcast over local Wi-Fi completes in <200ms, voice-to-voice end-to-end latency ≤3s SLA is NOT guaranteed for arbitrary-length spoken sentences.
3. **Translation Capability:** Arbitrary open-domain speech offline relies on pre-seeded local domain dictionary entries. Unseeded arbitrary sentences require backend connectivity.
