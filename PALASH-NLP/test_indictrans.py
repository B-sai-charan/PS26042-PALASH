import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor

MODEL_NAME = "ai4bharat/indictrans2-en-indic-1B"

device = "cuda" if torch.cuda.is_available() else "cpu"

print("Device:", device)

print("Loading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True
)

print("Loading model...")
model = AutoModelForSeq2SeqLM.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True,
    torch_dtype=torch.float16 if device == "cuda" else torch.float32,
)

model = model.to(device)
model.eval()

print("Initializing IndicProcessor...")
ip = IndicProcessor(inference=True)

src_lang = "eng_Latn"
tgt_lang = "hin_Deva"

text = "Hello, how are you?"

batch = ip.preprocess_batch(
    [text],
    src_lang=src_lang,
    tgt_lang=tgt_lang,
)

inputs = tokenizer(
    batch,
    truncation=True,
    padding="longest",
    return_tensors="pt",
).to(device)

print("Translating...")

with torch.no_grad():
    generated_tokens = model.generate(
        **inputs,
        max_length=256,
        num_beams=1,
    )

decoded = tokenizer.batch_decode(
    generated_tokens,
    skip_special_tokens=True,
)

output = ip.postprocess_batch(
    decoded,
    lang=tgt_lang,
)

print()
print("English:", text)
print("Hindi  :", output[0])