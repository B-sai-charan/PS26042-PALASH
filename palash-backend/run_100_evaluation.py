import json
import time

from app.services.translation_service import translate

INPUT_FILE = "palash_evaluation_dataset.json"
OUTPUT_FILE = "palash_evaluation_results.json"

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    dataset = json.load(f)

results = []

print("=" * 80)
print("PALASH 100-SENTENCE TRANSLATION EVALUATION")
print("=" * 80)
print("Total entries:", len(dataset))
print()

for item in dataset:
    print(f"[{item['id']}/100] Translating...")

    result = translate(
        item["source_text"],
        source_language=item["source_language"],
        target_language=item["target_language"]
    )

    item["translated_text"] = result.get("translated_text")
    item["latency_ms"] = result.get("latency_ms")
    item["status"] = result.get("status")
    item["error"] = result.get("error")

    results.append(item)

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
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
    if r.get("latency_ms") is not None
]

print()
print("=" * 80)
print("EVALUATION COMPLETE")
print("=" * 80)
print("Successful :", len(successful), "/", len(results))

if latencies:
    print("Average    :", round(sum(latencies) / len(latencies), 2), "ms")
    print("Minimum    :", round(min(latencies), 2), "ms")
    print("Maximum    :", round(max(latencies), 2), "ms")

print()
print("Saved:", OUTPUT_FILE)
print("=" * 80)
