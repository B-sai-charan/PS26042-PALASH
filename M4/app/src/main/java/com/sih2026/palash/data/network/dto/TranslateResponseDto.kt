package com.sih2026.palash.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * M1 Backend Translate Response DTO.
 * Contract:
 * {
 *   "translated_text": "ᱥᱚᱨᱠᱟᱨ ᱟᱜ ᱢᱤᱫᱴᱟᱝ ᱯᱚᱨᱤᱢᱟᱱ ᱠᱷᱚᱱ ᱪᱟᱥᱤᱭᱟᱹ ᱠᱚ ᱵᱷᱟᱞᱮ ᱧᱟᱢᱼᱟ ᱾",
 *   "source_language": "hin_Deva",
 *   "target_language": "sat_Olck",
 *   "latency_ms": 3719.94,
 *   "status": "success"
 * }
 */
data class TranslateResponseDto(
    @SerializedName("translated_text") val translatedText: String,
    @SerializedName("source_language") val sourceLanguage: String,
    @SerializedName("target_language") val targetLanguage: String,
    @SerializedName("latency_ms") val latencyMs: Double,
    @SerializedName("status") val status: String
) {
    /**
     * Validates that the response strictly satisfies the frozen M1 contract requirements:
     * - translated_text is non-blank
     * - source_language is hin_Deva
     * - target_language is sat_Olck
     * - status is success
     * - latency_ms is non-negative
     */
    fun isValid(): Boolean {
        return translatedText.isNotBlank() &&
                sourceLanguage == "hin_Deva" &&
                targetLanguage == "sat_Olck" &&
                status == "success" &&
                latencyMs >= 0.0
    }
}
