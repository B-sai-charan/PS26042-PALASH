import json

with open("translation_results_review.json", "r", encoding="utf-8") as f:
    results = json.load(f)

print("=" * 100)
print("PALASH TRANSLATION HUMAN QUALITY REVIEW")
print("=" * 100)

for item in results:
    print(f"\n[{item['id']}]")
    print("Hindi   :", item["source_text"])
    print("Santali :", item["translated_text"])
    print()

    while True:
        score = input("Quality score (1-5): ").strip()
        if score in {"1", "2", "3", "4", "5"}:
            item["quality_score"] = int(score)
            break
        print("Enter a number from 1 to 5.")

    while True:
        meaning = input("Meaning preserved? (y/n): ").strip().lower()
        if meaning in {"y", "n"}:
            item["meaning_preserved"] = meaning == "y"
            break
        print("Enter y or n.")

    item["notes"] = input("Notes: ").strip()

with open("translation_results_review.json", "w", encoding="utf-8") as f:
    json.dump(results, f, ensure_ascii=False, indent=2)

scores = [x["quality_score"] for x in results]
preserved = [x["meaning_preserved"] for x in results]

print("\n" + "=" * 100)
print("QUALITY REVIEW SUMMARY")
print("=" * 100)
print("Entries reviewed :", len(results))
print("Average score    :", round(sum(scores) / len(scores), 2), "/ 5")
print("Meaning preserved:", sum(preserved), "/", len(preserved))
print("Meaning accuracy :", round(sum(preserved) / len(preserved) * 100, 2), "%")
print("Saved            : translation_results_review.json")
print("=" * 100)
