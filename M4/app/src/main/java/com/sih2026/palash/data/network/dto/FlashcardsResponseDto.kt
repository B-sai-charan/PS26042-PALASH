package com.sih2026.palash.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * M1 Backend Flashcards Response DTO wrapper.
 */
data class FlashcardsResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("count") val count: Int,
    @SerializedName("flashcards") val flashcards: List<FlashcardDto>
)

/**
 * M1 Backend Single Flashcard DTO.
 * Contract:
 * {
 *   "id": 1,
 *   "lesson_id": 1,
 *   "front_text": "1",
 *   "back_text": "᱑",
 *   "language": "hin_Deva-sat_Olck",
 *   "version": 1,
 *   "created_at": "...",
 *   "updated_at": "..."
 * }
 */
data class FlashcardDto(
    @SerializedName("id") val id: Int,
    @SerializedName("lesson_id") val lessonId: Int,
    @SerializedName("front_text") val frontText: String,
    @SerializedName("back_text") val backText: String,
    @SerializedName("language") val language: String,
    @SerializedName("version") val version: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
