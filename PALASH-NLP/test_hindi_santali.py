import os
import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor

# ---------------------------------------------------------
# Configuration
# ---------------------------------------------------------

MODEL_NAME = "ai4bharat/indictrans2-en-indic-1B"

SRC_LANG = "hin_Deva"   # Hindi
TGT_LANG = "sat_Olck"   # Santali (Ol Chiki)

device = "cuda" if torch.cuda.is_available() else "cpu"

print("=" * 60)
print("Hindi → Santali Translator")
print("=" * 60)
print("Device:", device)

if device == "cuda":
    print("GPU:", torch.cuda.get_device_name(0))
    print(
        "GPU Memory:",
        round(torch.cuda.get_device_properties(0).total_memory / 1024**3, 2),
        "GB",
    )

# ---------------------------------------------------------
# Load tokenizer
# ---------------------------------------------------------

print("\nLoading tokenizer...")

tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True,
)

print("Tokenizer loaded.")

# ---------------------------------------------------------
# Load model
# ---------------------------------------------------------

print("\nLoading model...")

model = AutoModelForSeq2SeqLM.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True,
    torch_dtype=torch.float16 if device == "cuda" else torch.float32,
)

model = model.to(device)
model.eval()

print("Model loaded successfully.")

# ---------------------------------------------------------
# Indic Processor
# ---------------------------------------------------------

print("\nInitializing IndicProcessor...")

ip = IndicProcessor(inference=True)

print("IndicProcessor ready.")

# ---------------------------------------------------------
# Translation function
# ---------------------------------------------------------

def translate_hindi_to_santali(text):

    batch = ip.preprocess_batch(
        [text],
        src_lang=SRC_LANG,
        tgt_lang=TGT_LANG,
    )

    inputs = tokenizer(
        batch,
        truncation=True,
        padding="longest",
        return_tensors="pt",
    )

    inputs = {key: value.to(device) for key, value in inputs.items()}

    with torch.no_grad():

        generated_tokens = model.generate(
            **inputs,
            max_length=256,
            num_beams=5,
            early_stopping=True,
        )

    decoded = tokenizer.batch_decode(
        generated_tokens,
        skip_special_tokens=True,
    )

    output = ip.postprocess_batch(
        decoded,
        lang=TGT_LANG,
    )

    return output[0]


# ---------------------------------------------------------
# TEST SENTENCES
# ---------------------------------------------------------

test_sentences = [

    "नमस्ते, आप कैसे हैं?",

    "नौ किसान खेत में काम कर रहे हैं।",

    "सरकार ने एक नई कृषि योजना की घोषणा की है।",

    "किसान इस योजना के लिए ऑनलाइन आवेदन कर सकते हैं।",

    "कृपया निकटतम कार्यालय में जाएं।",

    "आज मौसम बहुत अच्छा है।",

    "कल बारिश होने की संभावना है।",

    "हमारे समाज के विकास के लिए शिक्षा महत्वपूर्ण है।",

    "यह योजना किसानों के लिए बहुत उपयोगी है।",

    "सरकार किसानों को आर्थिक सहायता प्रदान करेगी.",
]


print("\n")
print("=" * 60)
print("HINDI → SANTALI TEST")
print("=" * 60)

for i, text in enumerate(test_sentences, 1):

    print(f"\nTest {i}")
    print("-" * 60)

    print("Hindi:")
    print(text)

    try:

        result = translate_hindi_to_santali(text)

        print("\nSantali:")
        print(result)

    except Exception as e:

        print("\nERROR:")
        print(type(e).__name__, str(e))


# ---------------------------------------------------------
# Interactive translator
# ---------------------------------------------------------

print("\n")
print("=" * 60)
print("INTERACTIVE HINDI → SANTALI TRANSLATOR")
print("=" * 60)

print("Type Hindi text and press Enter.")
print("Type 'exit' to quit.")

while True:

    try:

        text = input("\nHindi: ").strip()

        if text.lower() == "exit":
            print("Exiting translator.")
            break

        if not text:
            continue

        result = translate_hindi_to_santali(text)

        print("Santali:", result)

    except KeyboardInterrupt:

        print("\nExiting translator.")
        break

    except Exception as e:

        print("ERROR:", type(e).__name__, str(e))