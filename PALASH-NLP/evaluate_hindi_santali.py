import os
import csv
import torch
import sacrebleu

from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
from IndicTransToolkit.processor import IndicProcessor


# ============================================================
# CONFIGURATION
# ============================================================

MODEL_NAME = "ai4bharat/indictrans2-indic-indic-1B"

DATASET_PATH = os.path.join(
    "data",
    "hindi_santali_test.csv"
)

SRC_LANG = "hin_Deva"
TGT_LANG = "sat_Olck"


# ============================================================
# DEVICE
# ============================================================

device = "cuda" if torch.cuda.is_available() else "cpu"

print("=" * 60)
print("Hindi → Santali Evaluation")
print("=" * 60)

print("Device:", device)

if device == "cuda":
    print("GPU:", torch.cuda.get_device_name(0))

    gpu_memory = torch.cuda.get_device_properties(0).total_memory / (1024 ** 3)
    print(f"GPU Memory: {gpu_memory:.1f} GB")


# ============================================================
# LOAD TOKENIZER
# ============================================================

print("\nLoading tokenizer...")

tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True
)

print("Tokenizer loaded.")


# ============================================================
# LOAD MODEL
# ============================================================

print("\nLoading model...")

model = AutoModelForSeq2SeqLM.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True,
    torch_dtype=torch.float16
)

model = model.to(device)
model.eval()

print("Model loaded.")


# ============================================================
# PROCESSOR
# ============================================================

print("\nInitializing IndicProcessor...")

processor = IndicProcessor(
    inference=True
)

print("Processor ready.")


# ============================================================
# READ DATASET
# ============================================================

print("\nReading dataset...")

if not os.path.exists(DATASET_PATH):
    raise FileNotFoundError(
        f"\nDataset not found:\n{DATASET_PATH}\n\n"
        f"Make sure your CSV is located at:\n"
        f"data\\hindi_santali_test.csv"
    )


hindi_sentences = []
reference_sentences = []


with open(
    DATASET_PATH,
    "r",
    encoding="utf-8-sig",
    newline=""
) as file:

    reader = csv.DictReader(file)

    print("CSV columns:", reader.fieldnames)

    if reader.fieldnames is None:
        raise ValueError("CSV has no header.")

    # Remove accidental spaces from column names
    reader.fieldnames = [
        field.strip()
        for field in reader.fieldnames
    ]

    if "Hindi" not in reader.fieldnames:
        raise ValueError(
            "CSV must contain a column named 'Hindi'. "
            f"Found: {reader.fieldnames}"
        )

    if "Santali" not in reader.fieldnames:
        raise ValueError(
            "CSV must contain a column named 'Santali'. "
            f"Found: {reader.fieldnames}"
        )

    for row in reader:

        hindi = row["Hindi"].strip()
        santali = row["Santali"].strip()

        if not hindi or not santali:
            continue

        hindi_sentences.append(hindi)
        reference_sentences.append(santali)


print(f"Loaded {len(hindi_sentences)} sentence pairs.")


if len(hindi_sentences) == 0:
    raise ValueError("No valid sentence pairs found in CSV.")


# ============================================================
# TRANSLATION FUNCTION
# ============================================================

def translate_batch(sentences):

    processed = processor.preprocess_batch(
        sentences,
        src_lang=SRC_LANG,
        tgt_lang=TGT_LANG
    )

    inputs = tokenizer(
        processed,
        padding=True,
        truncation=True,
        return_tensors="pt"
    )

    inputs = {
        key: value.to(device)
        for key, value in inputs.items()
    }

    with torch.inference_mode():

        generated_tokens = model.generate(
            **inputs,
            max_length=256,
            num_beams=5,
            early_stopping=True
        )

    decoded = tokenizer.batch_decode(
        generated_tokens,
        skip_special_tokens=True
    )

    translations = processor.postprocess_batch(
        decoded,
        lang=TGT_LANG
    )

    return translations


# ============================================================
# TRANSLATE DATASET
# ============================================================

print("\nTranslating Hindi → Santali...")

predictions = []

BATCH_SIZE = 4

for i in range(0, len(hindi_sentences), BATCH_SIZE):

    batch = hindi_sentences[
        i:i + BATCH_SIZE
    ]

    batch_predictions = translate_batch(batch)

    predictions.extend(batch_predictions)

    print(
        f"Translated "
        f"{min(i + BATCH_SIZE, len(hindi_sentences))}"
        f"/{len(hindi_sentences)}"
    )


# ============================================================
# DISPLAY RESULTS
# ============================================================

print("\n")
print("=" * 60)
print("TRANSLATION RESULTS")
print("=" * 60)

for i, (src, ref, pred) in enumerate(
    zip(
        hindi_sentences,
        reference_sentences,
        predictions
    ),
    start=1
):

    print(f"\n[{i}]")
    print("Hindi   :", src)
    print("Reference Santali:")
    print(ref)
    print("Model Santali:")
    print(pred)


# ============================================================
# BLEU
# ============================================================

print("\n")
print("=" * 60)
print("AUTOMATIC EVALUATION")
print("=" * 60)

bleu = sacrebleu.corpus_bleu(
    predictions,
    [reference_sentences]
)


# ============================================================
# chrF
# ============================================================

chrf = sacrebleu.corpus_chrf(
    predictions,
    [reference_sentences]
)


# ============================================================
# RESULTS
# ============================================================

print("\nBLEU Score :", round(bleu.score, 2))
print("chrF Score :", round(chrf.score, 2))

print("\n")
print("=" * 60)
print("EVALUATION COMPLETE")
print("=" * 60)