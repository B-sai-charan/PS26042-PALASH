import requests
import time
import json

URL = "http://127.0.0.1:8000/translate"

tests = [
    "किसानों को सरकारी योजना का लाभ मिलेगा।",
    "किसान सरकारी योजना के लिए आवेदन कर सकते हैं।",
    "सरकार किसानों को आर्थिक सहायता प्रदान करेगी।",
    "यह योजना छोटे और सीमांत किसानों के लिए है।",
    "लाभ प्राप्त करने के लिए किसानों को आवश्यक दस्तावेज जमा करने होंगे।",
]

results = []

print("=" * 80)
print("PALASH API TRANSLATION BENCHMARK")
print("=" * 80)

for i, text in enumerate(tests, 1):

    payload = {
        "text": text,
        "source_language": "hin_Deva",
        "target_language": "sat_Olck"
    }

    start = time.perf_counter()

    try:
        response = requests.post(
            URL,
            json=payload,
            timeout=30
        )

        elapsed = (time.perf_counter() - start) * 1000

        data = response.json()

        result = {
            "id": i,
            "http_status": response.status_code,
            "latency_ms": round(elapsed, 2),
            "model_latency_ms": data.get("latency_ms"),
            "status": data.get("status"),
            "source_text": text,
            "translated_text": data.get("translated_text"),
            "error": data.get("error")
        }

        results.append(result)

        print(f"\n[{i}]")
        print("HTTP       :", response.status_code)
        print("API latency:", round(elapsed, 2), "ms")
        print("Model      :", data.get("latency_ms"), "ms")
        print("Status     :", data.get("status"))
        print("Hindi      :", text)
        print("Santali    :", data.get("translated_text"))

    except Exception as e:

        print(f"\n[{i}] ERROR")
        print(str(e))

        results.append({
            "id": i,
            "status": "error",
            "error": str(e)
        })


with open(
    "api_translation_benchmark.json",
    "w",
    encoding="utf-8"
) as f:
    json.dump(
        results,
        f,
        ensure_ascii=False,
        indent=2
    )

successful = [
    r for r in results
    if r.get("status") == "success"
]

latencies = [
    r["latency_ms"]
    for r in successful
]

print("\n" + "=" * 80)
print("API BENCHMARK SUMMARY")
print("=" * 80)

print(
    "Successful:",
    len(successful),
    "/",
    len(results)
)

if latencies:
    print(
        "Average API latency:",
        round(sum(latencies) / len(latencies), 2),
        "ms"
    )

    print(
        "Minimum API latency:",
        round(min(latencies), 2),
        "ms"
    )

    print(
        "Maximum API latency:",
        round(max(latencies), 2),
        "ms"
    )

print("\nSaved: api_translation_benchmark.json")
print("=" * 80)
