import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor


MODEL_NAME = "ai4bharat/indictrans2-en-indic-1B"

device = "cuda" if torch.cuda.is_available() else "cpu"

print(f"Using device: {device}")

tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True
)

model = AutoModelForSeq2SeqLM.from_pretrained(
    MODEL_NAME,
    trust_remote_code=True,
    torch_dtype=torch.float16 if device == "cuda" else torch.float32,
)

model = model.to(device)
model.eval()

ip = IndicProcessor(inference=True)


def translate(text, tgt_lang="hin_Deva"):
    batch = ip.preprocess_batch(
        [text],
        src_lang="eng_Latn",
        tgt_lang=tgt_lang,
    )

    inputs = tokenizer(
        batch,
        truncation=True,
        padding="longest",
        return_tensors="pt",
    ).to(device)

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

    return output[0]