import json
import time

from app.services.translation_service import translate


INPUT_FILE = "palash_evaluation_dataset.json"
OUTPUT_FILE = "palash_evaluation_dataset_translated.json"


with open(INPUT_FILE, "r", encoding="utf-8") as f:
    dataset = json.load(f)


print("=" * 80)
print("PALASH 100-SENTENCE TRANSLATION EVALUATION")
print("=" * 80)

successful = 0
failed = 0
latencies = []


for item in dataset:

    print(f"\n[{item['id']}/100]")
    print("Hindi   :", item["source_text"])

    result = translate(
        item["source_text"],
        source_language="hin_Deva",
        target_language="sat_Olck"
    )

    item["translated_text"] = result.get("translated_text")

    if result["status"] == "success":
        successful += 1
        latencies.append(result["latency_ms"])

        print("Santali :", result["translated_text"])
        print("Latency :", result["latency_ms"], "ms")

    else:
        failed += 1

        print("ERROR   :", result.get("error"))

    time.sleep(0.05)


with open(
    OUTPUT_FILE,
    "w",
    encoding="utf-8"
) as f:
    json.dump(
        dataset,
        f,
        ensure_ascii=False,
        indent=2
    )


print("\n" + "=" * 80)
print("TRANSLATION EVALUATION COMPLETE")
print("=" * 80)

print("Total      :", len(dataset))
print("Successful :", successful)
print("Failed     :", failed)

if latencies:
    print("Average    :", round(sum(latencies) / len(latencies), 2), "ms")
    print("Minimum    :", round(min(latencies), 2), "ms")
    print("Maximum    :", round(max(latencies), 2), "ms")

print("\nSaved:", OUTPUT_FILE)
print("=" * 80)
