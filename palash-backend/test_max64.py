import json
import re
import collections
import time
import torch
from app.services.translation_service import tokenizer, model, processor, DEVICE

with open("palash_evaluation_results.json", encoding="utf-8") as f:
    results = json.load(f)

bad = []
start = time.perf_counter()

for x in results:
    p = processor.preprocess_batch(
        [x["source_text"]],
        src_lang="hin_Deva",
        tgt_lang="sat_Olck"
    )

    inputs = tokenizer(
        p,
        padding=True,
        truncation=True,
        max_length=256,
        return_tensors="pt"
    )

    inputs = {k: v.to(DEVICE) for k, v in inputs.items()}

    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_new_tokens=64,
            num_beams=5,
            do_sample=False,
            no_repeat_ngram_size=3
        )

    decoded = tokenizer.batch_decode(
        outputs,
        skip_special_tokens=True
    )

    translated = processor.postprocess_batch(
        decoded,
        lang="sat_Olck"
    )[0]

    ngrams = [
        translated[i:i+3]
        for i in range(len(translated) - 2)
    ]

    max_repeat = max(
        collections.Counter(ngrams).values()
    ) if ngrams else 0

    non_olchiki = len(
        re.findall(r'[^\u1C50-\u1C7F\s\u0964]', translated)
    )

    if max_repeat > 10 or non_olchiki > 0:
        bad.append(
            (x["id"], max_repeat, non_olchiki, len(translated))
        )

print("=" * 70)
print("BEAM 5 / MAX_NEW_TOKENS 64 - 100 SENTENCE TEST")
print("=" * 70)
print("TOTAL:", len(results))
print("BAD:", len(bad))

for item in bad:
    print(item)

print("TOTAL_TIME_SEC:", round(time.perf_counter() - start, 2))
