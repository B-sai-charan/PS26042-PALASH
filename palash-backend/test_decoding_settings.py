import time
import torch

from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
from IndicTransToolkit import IndicProcessor

MODEL_NAME = "ai4bharat/indictrans2-indic-indic-1B"

SRC = "hin_Deva"
TGT = "sat_Olck"

TEXT = "बैंक खाते की जानकारी गलत नहीं होनी चाहिए।"

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

print("Loading model on", DEVICE)

tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True
)

if DEVICE == "cuda":
    model = AutoModelForSeq2SeqLM.from_pretrained(
        MODEL_NAME,
        trust_remote_code=True,
        torch_dtype=torch.float16
    )
else:
    model = AutoModelForSeq2SeqLM.from_pretrained(
        MODEL_NAME,
        trust_remote_code=True
    )

model = model.to(DEVICE)
model.eval()

processor = IndicProcessor(inference=True)

processed = processor.preprocess_batch(
    [TEXT],
    src_lang=SRC,
    tgt_lang=TGT
)

inputs = tokenizer(
    processed,
    padding=True,
    truncation=True,
    max_length=256,
    return_tensors="pt"
)

inputs = {
    key: value.to(DEVICE)
    for key, value in inputs.items()
}

print()
print("=" * 100)
print("DECODING EXPERIMENT")
print("=" * 100)
print("Input:", TEXT)

settings = [
    {
        "name": "beam_1",
        "num_beams": 1,
        "do_sample": False
    },
    {
        "name": "beam_3",
        "num_beams": 3,
        "do_sample": False
    },
    {
        "name": "beam_5",
        "num_beams": 5,
        "do_sample": False
    },
    {
        "name": "beam_8",
        "num_beams": 8,
        "do_sample": False
    },
]

for setting in settings:

    if DEVICE == "cuda":
        torch.cuda.synchronize()

    start = time.perf_counter()

    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_length=128,
            num_beams=setting["num_beams"],
            do_sample=setting["do_sample"],
            early_stopping=True
        )

    if DEVICE == "cuda":
        torch.cuda.synchronize()

    latency = (time.perf_counter() - start) * 1000

    decoded = tokenizer.batch_decode(
        outputs,
        skip_special_tokens=True
    )

    result = processor.postprocess_batch(
        decoded,
        lang=TGT
    )[0]

    print()
    print("-" * 100)
    print("Setting :", setting["name"])
    print("Latency :", round(latency, 2), "ms")
    print("Output  :", result)

print()
print("=" * 100)
print("EXPERIMENT COMPLETE")
print("=" * 100)
