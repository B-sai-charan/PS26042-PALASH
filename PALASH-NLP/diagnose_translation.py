import time
import torch
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
from IndicTransToolkit import IndicProcessor

# =========================================================
# CONFIGURATION
# =========================================================

MODEL_NAME = "ai4bharat/indictrans2-indic-indic-1B"

SRC_LANG = "hin_Deva"
TGT_LANG = "sat_Olck"

device = "cuda" if torch.cuda.is_available() else "cpu"

print("=" * 60)
print("DIRECT HINDI -> SANTALI MODEL DIAGNOSTIC")
print("=" * 60)

print("Device:", device)

if device == "cuda":
    print("GPU:", torch.cuda.get_device_name(0))
    print(
        "GPU Memory:",
        round(
            torch.cuda.get_device_properties(0).total_memory / 1024**3,
            2
        ),
        "GB"
    )

# =========================================================
# TOKENIZER
# =========================================================

print("\nLoading tokenizer...")

tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True
)

print("Tokenizer loaded.")

# =========================================================
# MODEL
# =========================================================

print("\nLoading model...")

model = AutoModelForSeq2SeqLM.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True,
    torch_dtype=torch.float16 if device == "cuda" else torch.float32
)

model = model.to(device)
model.eval()

print("Model loaded.")

# =========================================================
# PROCESSOR
# =========================================================

print("\nLoading processor...")

processor = IndicProcessor(inference=True)

print("Processor loaded.")

# =========================================================
# TEST SENTENCES
# =========================================================

test_sentences = [
    "किसान खेत में काम कर रहा है।",
    "नमस्ते, आप कैसे हैं?",
    "सरकार ने एक नई कृषि योजना की घोषणा की है।",
    "कृपया निकटतम कार्यालय में जाएँ।",
    "आज मौसम बहुत अच्छा है।"
]

# =========================================================
# TRANSLATION FUNCTION
# =========================================================

def translate(text):

    processed = processor.preprocess_batch(
        [text],
        src_lang=SRC_LANG,
        tgt_lang=TGT_LANG
    )

    inputs = tokenizer(
        processed,
        padding=True,
        truncation=True,
        max_length=256,
        return_tensors="pt"
    )

    inputs = {
        key: value.to(device)
        for key, value in inputs.items()
    }

    # CUDA synchronization gives us a more accurate timing
    if device == "cuda":
        torch.cuda.synchronize()

    start = time.perf_counter()

    with torch.inference_mode():

        output = model.generate(
            **inputs,

            # IMPORTANT:
            # Greedy decoding is much faster on your RTX 3060
            num_beams=1,

            max_new_tokens=64,

            do_sample=False,

            use_cache=True
        )

    if device == "cuda":
        torch.cuda.synchronize()

    elapsed = time.perf_counter() - start

    decoded = tokenizer.batch_decode(
        output,
        skip_special_tokens=True
    )

    result = processor.postprocess_batch(
        decoded,
        lang=TGT_LANG
    )

    return result[0], elapsed


# =========================================================
# RUN TESTS
# =========================================================

print("\n")
print("=" * 60)
print("HINDI -> SANTALI TRANSLATION TEST")
print("=" * 60)

for i, text in enumerate(test_sentences, 1):

    print(f"\nTEST {i}")
    print("-" * 60)

    print("Hindi:")
    print(text)

    try:

        result, elapsed = translate(text)

        print("\nSantali:")
        print(result)

        print(
            "\nInference time:",
            round(elapsed, 2),
            "seconds"
        )

    except Exception as e:

        print("\nERROR:")
        print(type(e).__name__, str(e))


# =========================================================
# GPU MEMORY
# =========================================================

if device == "cuda":

    allocated = torch.cuda.memory_allocated() / 1024**3
    reserved = torch.cuda.memory_reserved() / 1024**3

    print("\n")
    print("=" * 60)
    print("GPU MEMORY")
    print("=" * 60)

    print("Allocated:", round(allocated, 2), "GB")
    print("Reserved :", round(reserved, 2), "GB")


print("\n")
print("=" * 60)
print("DIAGNOSTIC COMPLETE")
print("=" * 60)