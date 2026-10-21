import json
import re

with open("palash_evaluation_results.json", encoding="utf-8") as f:
    results = json.load(f)

print("=" * 100)
print("PALASH AUTOMATIC TRANSLATION RED-FLAG REPORT")
print("=" * 100)

hindi_pattern = re.compile(r'[\u0900-\u097F]')
latin_pattern = re.compile(r'[A-Za-z]')

for r in results:

    text = r.get("translated_text") or ""

    flags = []

    if hindi_pattern.search(text):
        flags.append("HINDI_LEAK")

    if latin_pattern.search(text):
        flags.append("LATIN_TEXT")

    if len(text.strip()) < 5:
        flags.append("VERY_SHORT")

    if len(text) > len(r["source_text"]) * 4:
        flags.append("VERY_LONG")

    if flags:
        print(f"\n[{r['id']}] {' | '.join(flags)}")
        print("Hindi   :", r["source_text"])
        print("Santali :", text)

print("\n" + "=" * 100)
print("RED-FLAG ANALYSIS COMPLETE")
print("=" * 100)
