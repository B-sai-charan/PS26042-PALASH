import time
import statistics

from app.services.translation_service import translate


tests = [
    "किसानों को सरकारी योजना का लाभ मिलेगा।",
    "किसान सरकारी योजना के लिए आवेदन कर सकते हैं।",
    "सरकार किसानों को आर्थिक सहायता प्रदान करेगी।",
    "यह योजना छोटे और सीमांत किसानों के लिए है।",
    "लाभ प्राप्त करने के लिए किसानों को आवश्यक दस्तावेज जमा करने होंगे।",
    "किसान अपने नजदीकी कार्यालय में आवेदन कर सकते हैं।",
    "आवेदन करने के 30 दिनों के भीतर लाभ मिल सकता है।",
    "इस योजना के तहत किसानों को बीज और खाद उपलब्ध कराई जाएगी।",
    "सरकारी योजना का लाभ प्राप्त करने के लिए आधार कार्ड आवश्यक है।",
    "किसानों को आवेदन की स्थिति ऑनलाइन जांचने की सुविधा मिलेगी।",
]

print("=" * 90)
print("PALASH HINDI -> SANTALI WARM-MODEL BENCHMARK")
print("=" * 90)

# ---------------------------------------------------------
# WARM-UP
# ---------------------------------------------------------

print("\nWarming up model...")

warmup_text = "किसानों को सरकारी योजना का लाभ मिलेगा।"

for i in range(2):
    result = translate(
        warmup_text,
        source_language="hin_Deva",
        target_language="sat_Olck",
    )
    print(
        f"Warm-up {i + 1}:",
        result.get("latency_ms"),
        "ms"
    )

# ---------------------------------------------------------
# BENCHMARK
# ---------------------------------------------------------

latencies = []

for i, text in enumerate(tests, 1):

    result = translate(
        text,
        source_language="hin_Deva",
        target_language="sat_Olck",
    )

    latency = result.get("latency_ms")

    if result.get("status") == "success":
        latencies.append(latency)

    print(f"\n[{i}]")
    print("Hindi   :", text)
    print("Santali :", result.get("translated_text"))
    print("Latency :", latency, "ms")
    print("Status  :", result.get("status"))

# ---------------------------------------------------------
# SUMMARY
# ---------------------------------------------------------

print("\n" + "=" * 90)
print("WARM-MODEL BENCHMARK SUMMARY")
print("=" * 90)

print("Successful :", len(latencies), "/", len(tests))

if latencies:

    print(
        "Average    :",
        round(statistics.mean(latencies), 2),
        "ms"
    )

    print(
        "Median     :",
        round(statistics.median(latencies), 2),
        "ms"
    )

    print(
        "Minimum    :",
        round(min(latencies), 2),
        "ms"
    )

    print(
        "Maximum    :",
        round(max(latencies), 2),
        "ms"
    )

    if len(latencies) >= 2:
        print(
            "Std Dev    :",
            round(statistics.stdev(latencies), 2),
            "ms"
        )

print("=" * 90)
