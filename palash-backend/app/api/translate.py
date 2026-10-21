from fastapi import APIRouter, HTTPException

from app.schemas.translation import TranslationRequest
from app.services.translation_service import translate


router = APIRouter(
    prefix="/translate",
    tags=["Translation"]
)


@router.post("")
def translate_text(request: TranslationRequest):

    result = translate(
        text=request.text,
        source_language=request.source_language,
        target_language=request.target_language
    )

    if result["status"] == "error":
        raise HTTPException(
            status_code=400,
            detail=result.get("error", "Translation failed")
        )

    return result