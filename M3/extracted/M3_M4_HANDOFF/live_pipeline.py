import sys
import os
import time

sys.path.append(
    os.path.dirname(
        os.path.dirname(
            os.path.abspath(__file__)
        )
    )
)

from translation_service import translate
from faster_whisper import WhisperModel
import sounddevice as sd
from scipy.io.wavfile import write
import pyttsx3

# -------------------------
# TOTAL TIMER
# -------------------------

total_start = time.time()

# -------------------------
# RECORD AUDIO
# -------------------------

fs = 16000

input("\nPress ENTER and speak for 5 seconds...")

print("\nRecording...")

audio = sd.rec(
    int(5 * fs),
    samplerate=fs,
    channels=1
)

sd.wait()

write(
    "audio/live_input.wav",
    fs,
    audio
)

print("Saved: audio/live_input.wav")

# -------------------------
# ASR
# -------------------------

print("\nLoading ASR model...")

asr_start = time.time()

model = WhisperModel(
    "small",
    device="cpu",
    compute_type="int8"
)

segments, info = model.transcribe(
    "audio/live_input.wav",
    language="hi"
)

hindi_text = ""

for segment in segments:
    hindi_text += segment.text

asr_latency = (time.time() - asr_start) * 1000

print("\nHindi Text:")
print(hindi_text)
with open(
    "logs/last_hindi.txt",
    "w",
    encoding="utf-8"
) as f:
    f.write(hindi_text)
print("\nASR Latency:")
print(round(asr_latency, 2), "ms")

# -------------------------
# TRANSLATION
# -------------------------

print("\nTranslating...")

result = translate(
    hindi_text,
    source_language="hin_Deva",
    target_language="sat_Olck"
)

if result["status"] != "success":
    print("\nTranslation Failed")
    print(result)
    exit()

santali_text = result["translated_text"]

print("\nSantali Text:")
print(santali_text)
with open(
    "logs/last_santali.txt",
    "w",
    encoding="utf-8"
) as f:
    f.write(santali_text)

print("\nTranslation Latency:")
print(result["latency_ms"], "ms")

# -------------------------
# TRANSLITERATION
# -------------------------

mapping = {
    "ᱚ": "o",
    "ᱛ": "t",
    "ᱜ": "g",
    "ᱝ": "ng",
    "ᱞ": "l",
    "ᱟ": "a",
    "ᱠ": "k",
    "ᱡ": "j",
    "ᱢ": "m",
    "ᱣ": "w",

    "ᱤ": "i",
    "ᱥ": "s",
    "ᱦ": "h",
    "ᱧ": "ny",
    "ᱨ": "r",
    "ᱩ": "u",
    "ᱪ": "ch",
    "ᱫ": "d",
    "ᱬ": "n",
    "ᱭ": "y",

    "ᱮ": "e",
    "ᱯ": "p",
    "ᱰ": "d",
    "ᱱ": "n",
    "ᱲ": "r",
    "ᱳ": "o",
    "ᱴ": "t",
    "ᱵ": "b",
    "ᱶ": "v",
    "ᱷ": "h",

    "ᱸ": "n",
    "ᱹ": "",
    "ᱺ": "",
    "ᱼ": "-",
    "ᱽ": "",

    "᱾": ".",

    ",": ",",
    "?": "?",
    "!": "!",
    ":": ":",
    ";": ";",
    "0": "zero",
    "1": "mit",
"2": "bar",
"3": "pe",
"4": "pon",
"5": "more",
"6": "turui",
"7": "eyae",
"8": "iral",
"9": "are",
}

roman_text = ""

for ch in santali_text:
    roman_text += mapping.get(ch, ch)
with open(
    "logs/last_roman.txt",
    "w",
    encoding="utf-8"
) as f:
    f.write(roman_text)
# Internal Romanization used only for TTS fallback
# Not shown during demo

for ch in set(santali_text):
    if ch not in mapping and not ch.isspace():
        print("UNMAPPED:", ch)

# -------------------------
# TTS
# -------------------------

tts_start = time.time()

print("\nSpeaking Output...")

import winsound

engine = pyttsx3.init()
engine.setProperty('rate', 90)

tts_start = time.perf_counter()

engine.save_to_file(
    roman_text,
    "audio/live_output.wav"
)

engine.runAndWait()

winsound.PlaySound(
    "audio/live_output.wav",
    winsound.SND_FILENAME
)
tts_latency = (
    time.perf_counter() - tts_start
) * 1000

print("\nTTS Latency:")
print(round(tts_latency,2),"ms")
total_latency = (
    time.time() - total_start
) * 1000

print("\nTOTAL LATENCY:")
print(round(total_latency, 2), "ms")

print("\nPipeline Complete")