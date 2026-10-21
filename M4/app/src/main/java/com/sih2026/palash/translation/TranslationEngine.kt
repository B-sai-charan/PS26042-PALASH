package com.sih2026.palash.translation

/**
 * Status of translation result.
 */
enum class TranslationStatus {
    SUCCESS,
    VERIFIED_LOCAL,
    UNAVAILABLE,
    ERROR
}

/**
 * Origin of translation result.
 */
enum class TranslationSourceType {
    BACKEND,
    LOCAL_CACHE,
    LOCAL_CONTENT_PACK,
    MODEL,
    UNKNOWN
}

/**
 * Data model for translation result.
 */
data class TranslationResult(
    val sourceText: String,
    val translatedText: String?,
    val sourceLanguage: String = "hi",
    val targetLanguage: String = "sat",
    val status: TranslationStatus,
    val sourceType: TranslationSourceType,
    val processingTimeMs: Long = 0L,
    val backendLatencyMs: Double? = null,
    val androidRoundTripMs: Long? = null,
    val errorMessage: String? = null
)

/**
 * Clean interface abstraction for Hindi -> Vernacular (Santali) translation.
 */
interface TranslationEngine {
    suspend fun translate(
        sourceText: String,
        sourceLanguage: String = "hi",
        targetLanguage: String = "sat"
    ): TranslationResult
}
