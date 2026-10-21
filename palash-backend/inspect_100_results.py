import json

with open("palash_evaluation_results.json", encoding="utf-8") as f:
    results = json.load(f)

print("=" * 100)
print("PALASH 100 TRANSLATION ERROR ANALYSIS")
print("=" * 100)

for r in results:
    print(f"\n[{r['id']}]")
    print("Hindi   :", r["source_text"])
    print("Santali :", r["translated_text"])
    print("Latency :", r.get("latency_ms"), "ms")

print("\n" + "=" * 100)
print("TOTAL:", len(results))
print("=" * 100)
