package com.sih2026.palash.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * M1 Backend Worksheets Response DTO wrapper.
 */
data class WorksheetsResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("count") val count: Int,
    @SerializedName("worksheets") val worksheets: List<WorksheetDto>
)

/**
 * M1 Backend Single Worksheet DTO.
 * Contract:
 * {
 *   "id": 1,
 *   "lesson_id": 1,
 *   "title": "Counting 1 to 20",
 *   "content": "...",
 *   "language": "hin_Deva-sat_Olck",
 *   "version": 1,
 *   "created_at": "...",
 *   "updated_at": "..."
 * }
 */
data class WorksheetDto(
    @SerializedName("id") val id: Int,
    @SerializedName("lesson_id") val lessonId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("language") val language: String,
    @SerializedName("version") val version: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
