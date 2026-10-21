# PALASH M3 → M4 Final TTS Handoff

## Objective

Target Demo Flow:

Hindi Speech
→ ASR
→ Hindi Text
→ Translation Service
→ Romanized Santali
→ Piper TTS
→ WAV Output
→ Android Playback

---

## Included Components

### Translation

translation_service.py

Provides Hindi → Santali translation service.

---

### Piper TTS

Model:

- en_IN-spicor-medium.onnx
- en_IN-spicor-medium.onnx.json

Reference Implementation:

- tts_piper_test.py

Current implementation generates WAV output from translated Santali text.

---

### Interface Definitions

- asr_output.json
- tts_interface.json
- tts_response.json
- translation_response.json

These define expected integration payloads.

---

## Audio Assets

Included:

- Numbers
- Flashcards
- Worksheets
- Assessments
- Grade 2 Lessons
- Grade 3 Assets

Updated Piper-generated WAV files.

---

## Android Notes

Current implementation is Python reference code.

Piper model is ONNX based.

Recommended Android approach:

ASR
→ Translation
→ ONNX Runtime Mobile
→ Piper Model
→ WAV Output
→ Android Audio Playback

Android feasibility validation required by M4.

---

## License

Hackathon internal use only.