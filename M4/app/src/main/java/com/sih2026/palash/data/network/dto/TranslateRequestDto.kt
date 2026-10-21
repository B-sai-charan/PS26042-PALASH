package com.sih2026.palash.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * M1 Backend Translate Request DTO.
 * Contract:
 * {
 *   "text": "किसानों को सरकारी योजना का लाभ मिलेगा।",
 *   "source_language": "hin_Deva",
 *   "target_language": "sat_Olck"
 * }
 */
data class TranslateRequestDto(
    @SerializedName("text") val text: String,
    @SerializedName("source_language") val sourceLanguage: String = "hin_Deva",
    @SerializedName("target_language") val targetLanguage: String = "sat_Olck"
)
