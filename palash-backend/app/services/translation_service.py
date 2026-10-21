import time
import torch

from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
from IndicTransToolkit import IndicProcessor


MODEL_NAME = "ai4bharat/indictrans2-indic-indic-1B"

DEFAULT_SOURCE_LANGUAGE = "hin_Deva"
DEFAULT_TARGET_LANGUAGE = "sat_Olck"

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"


print(f"Loading translation model on {DEVICE}...")

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

print("Translation model ready.")


SUPPORTED_SOURCE_LANGUAGES = {
    "hin_Deva"
}

SUPPORTED_TARGET_LANGUAGES = {
    "sat_Olck"
}


def translate(
    text,
    source_language=DEFAULT_SOURCE_LANGUAGE,
    target_language=DEFAULT_TARGET_LANGUAGE
):
    start_time = time.perf_counter()

    try:
        if not isinstance(text, str):
            raise ValueError("text must be a string")

        if not text.strip():
            raise ValueError("text cannot be empty")

        if source_language not in SUPPORTED_SOURCE_LANGUAGES:
            raise ValueError(
                f"Unsupported source language: {source_language}"
            )

        if target_language not in SUPPORTED_TARGET_LANGUAGES:
            raise ValueError(
                f"Unsupported target language: {target_language}"
            )

        processed = processor.preprocess_batch(
            [text],
            src_lang=source_language,
            tgt_lang=target_language
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

        if DEVICE == "cuda":
            torch.cuda.synchronize()

        with torch.inference_mode():
    	    outputs = model.generate(
            **inputs,
            max_new_tokens=128,
            num_beams=5,
            do_sample=False,
            early_stopping=True,
            no_repeat_ngram_size=3
        )

        if DEVICE == "cuda":
            torch.cuda.synchronize()

        decoded = tokenizer.batch_decode(
            outputs,
            skip_special_tokens=True
        )

        result = processor.postprocess_batch(
            decoded,
            lang=target_language
        )[0]

        latency_ms = round(
            (time.perf_counter() - start_time) * 1000,
            2
        )

        return {
            "translated_text": result,
            "source_language": source_language,
            "target_language": target_language,
            "latency_ms": latency_ms,
            "status": "success"
        }

    except Exception as e:
        latency_ms = round(
            (time.perf_counter() - start_time) * 1000,
            2
        )

        return {
            "translated_text": None,
            "source_language": source_language,
            "target_language": target_language,
            "latency_ms": latency_ms,
            "status": "error",
            "error": str(e)
        }