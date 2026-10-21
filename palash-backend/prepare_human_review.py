import json

INPUT_FILE = "palash_evaluation_results.json"
OUTPUT_FILE = "palash_human_review.json"

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    results = json.load(f)

for item in results:
    item["quality_score"] = None
    item["meaning_preserved"] = None
    item["notes"] = ""

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(
        results,
        f,
        ensure_ascii=False,
        indent=2
    )

print("=" * 80)
print("HUMAN REVIEW FILE CREATED")
print("=" * 80)
print("Entries:", len(results))
print("Saved :", OUTPUT_FILE)
print("=" * 80)
