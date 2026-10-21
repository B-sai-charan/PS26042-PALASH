import json

with open(
    "palash_evaluation_results.json",
    "r",
    encoding="utf-8"
) as f:
    data = json.load(f)

print("=" * 100)
print("PALASH 100-SENTENCE HUMAN QUALITY REVIEW")
print("=" * 100)

for item in data:

    print(f"\n[{item['id']}]")
    print("Hindi   :", item["source_text"])
    print("Santali :", item["translated_text"])

    while True:
        try:
            score = int(input("Quality score (1-5): "))
            if 1 <= score <= 5:
                break
        except ValueError:
            pass

        print("Enter a score between 1 and 5.")

    while True:
        meaning = input("Meaning preserved? (y/n): ").strip().lower()

        if meaning in ["y", "n"]:
            break

    notes = input("Notes: ").strip()

    item["quality_score"] = score
    item["meaning_preserved"] = meaning == "y"
    item["notes"] = notes

with open(
    "palash_evaluation_results_reviewed.json",
    "w",
    encoding="utf-8"
) as f:
    json.dump(
        data,
        f,
        ensure_ascii=False,
        indent=2
    )

scores = [
    x["quality_score"]
    for x in data
    if x["quality_score"] is not None
]

meaning_count = sum(
    1
    for x in data
    if x["meaning_preserved"]
)

print("\n" + "=" * 100)
print("QUALITY REVIEW SUMMARY")
print("=" * 100)

print("Entries reviewed :", len(scores))

if scores:
    print(
        "Average score    :",
        round(sum(scores) / len(scores), 2),
        "/ 5"
    )

print(
    "Meaning preserved:",
    meaning_count,
    "/",
    len(data)
)

print(
    "Meaning accuracy :",
    round(meaning_count / len(data) * 100, 2),
    "%"
)

print("=" * 100)