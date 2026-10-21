package com.sih2026.palash.data.network

import com.sih2026.palash.data.network.dto.FlashcardsResponseDto
import com.sih2026.palash.data.network.dto.HealthResponseDto
import com.sih2026.palash.data.network.dto.LessonsResponseDto
import com.sih2026.palash.data.network.dto.TranslateRequestDto
import com.sih2026.palash.data.network.dto.TranslateResponseDto
import com.sih2026.palash.data.network.dto.WorksheetsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interface contract for Member 1 Backend endpoints.
 * Explicitly implements verified GET /health, GET /lessons, GET /worksheets, GET /flashcards, and POST /translate APIs.
 */
interface PalashApiService {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponseDto>

    @GET("lessons")
    suspend fun getLessons(
        @Query("grade") grade: Int? = null,
        @Query("subject") subject: String? = null
    ): Response<LessonsResponseDto>

    @GET("worksheets")
    suspend fun getWorksheets(
        @Query("lesson_id") lessonId: Int? = null
    ): Response<WorksheetsResponseDto>

    @GET("flashcards")
    suspend fun getFlashcards(
        @Query("lesson_id") lessonId: Int? = null
    ): Response<FlashcardsResponseDto>

    @POST("translate")
    suspend fun translate(
        @Body request: TranslateRequestDto
    ): Response<TranslateResponseDto>
}

