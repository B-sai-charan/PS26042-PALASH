from pydantic import BaseModel


class TranslationRequest(BaseModel):
    text: str
    source_language: str = "hin_Deva"
    target_language: str = "sat_Olck"