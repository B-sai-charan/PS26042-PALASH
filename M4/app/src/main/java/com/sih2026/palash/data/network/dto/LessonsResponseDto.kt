package com.sih2026.palash.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * M1 Backend Lessons Response DTO wrapper.
 */
data class LessonsResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("count") val count: Int,
    @SerializedName("lessons") val lessons: List<LessonDto>
)

/**
 * M1 Backend Single Lesson DTO.
 * Contract:
 * {
 *   "id": 1,
 *   "grade": 2,
 *   "subject": "mathematics",
 *   "title": "Counting",
 *   "hindi_content": "1 से 20 तक गिनती सीखें।",
 *   "santali_content": "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾",
 *   "language": "hin_Deva-sat_Olck",
 *   "version": 1,
 *   "created_at": "...",
 *   "updated_at": "..."
 * }
 * 
 * Note: There is NO topic field in the M1 contract.
 */
data class LessonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("grade") val grade: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("title") val title: String,
    @SerializedName("hindi_content") val hindiContent: String?,
    @SerializedName("santali_content") val santaliContent: String?,
    @SerializedName("language") val language: String,
    @SerializedName("version") val version: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
