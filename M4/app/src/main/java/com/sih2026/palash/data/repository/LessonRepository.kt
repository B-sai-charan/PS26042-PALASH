package com.sih2026.palash.data.repository

import com.google.gson.JsonObject
import com.sih2026.palash.data.local.dao.LessonDao
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.network.ConnectivityObserver
import com.sih2026.palash.data.network.NetworkClient
import com.sih2026.palash.data.network.NetworkConfig
import com.sih2026.palash.data.network.NetworkConnectivityState
import com.sih2026.palash.data.network.NetworkErrorMessage
import com.sih2026.palash.data.network.NetworkResult
import com.sih2026.palash.data.network.dto.LessonDto
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    fun getAllLessons(): Flow<List<LessonEntity>>
    fun getLessonsByGradeAndSubject(grade: String, subject: String): Flow<List<LessonEntity>>
    suspend fun getLessonById(id: String): LessonEntity?
    suspend fun insertLesson(lesson: LessonEntity)
    suspend fun insertLessons(lessons: List<LessonEntity>)
    suspend fun syncLessonsFromBackend(grade: Int? = null, subject: String? = null): NetworkResult<List<LessonEntity>>
}

/**
 * Validates backend LessonDto according to M1 data validation rules.
 */
fun LessonDto.isValid(): Boolean {
    return id > 0 &&
            grade > 0 &&
            subject.isNotBlank() &&
            title.isNotBlank() &&
            language.isNotBlank() &&
            version > 0
}

/**
 * Safely maps a validated LessonDto to Room LessonEntity.
 */
fun LessonDto.toEntity(): LessonEntity {
    val jsonObject = JsonObject().apply {
        addProperty("hindi_content", hindiContent)
        addProperty("santali_content", santaliContent)
        addProperty("version", version)
        addProperty("created_at", createdAt)
        addProperty("updated_at", updatedAt)
    }

    val parsedCreatedAtMs = try {
        if (!createdAt.isNullOrBlank()) {
            java.time.Instant.parse(createdAt).toEpochMilli()
        } else {
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val formattedGrade = if (grade > 0) "Grade $grade" else "Grade 2"

    return LessonEntity(
        id = "backend_lesson_$id",
        grade = formattedGrade,
        subject = subject,
        topic = title,
        title = title,
        languageCode = language,
        contentJson = jsonObject.toString(),
        createdAt = parsedCreatedAtMs
    )
}

class LessonRepositoryImpl(
    private val lessonDao: LessonDao,
    private val connectivityObserver: ConnectivityObserver? = null
) : LessonRepository {

    override fun getAllLessons(): Flow<List<LessonEntity>> = lessonDao.getAllLessons()

    override fun getLessonsByGradeAndSubject(grade: String, subject: String): Flow<List<LessonEntity>> =
        lessonDao.getLessonsByGradeAndSubject(grade, subject)

    override suspend fun getLessonById(id: String): LessonEntity? = lessonDao.getLessonById(id)

    override suspend fun insertLesson(lesson: LessonEntity) = lessonDao.insertLesson(lesson)

    override suspend fun insertLessons(lessons: List<LessonEntity>) = lessonDao.insertLessons(lessons)

    override suspend fun syncLessonsFromBackend(
        grade: Int?,
        subject: String?
    ): NetworkResult<List<LessonEntity>> {
        if (!NetworkConfig.isConfigured) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)
        }

        if (connectivityObserver != null && connectivityObserver.getCurrentState() == NetworkConnectivityState.OFFLINE) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.INTERNET_UNAVAILABLE)
        }

        val apiService = NetworkClient.getApiService()
            ?: return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)

        return try {
            val response = apiService.getLessons(grade = grade, subject = subject)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.lessons.isNotEmpty()) {
                    val validEntities = body.lessons
                        .filter { it.isValid() }
                        .map { it.toEntity() }

                    if (validEntities.isNotEmpty()) {
                        lessonDao.insertLessons(validEntities)
                    }

                    NetworkResult.Success(validEntities)
                } else {
                    NetworkResult.Success(emptyList())
                }
            } else {
                NetworkResult.HttpError(response.code(), response.message())
            }
        } catch (e: java.net.SocketTimeoutException) {
            NetworkResult.Timeout()
        } catch (e: java.io.IOException) {
            NetworkResult.NetworkUnavailable(e.message ?: NetworkErrorMessage.SERVER_UNREACHABLE)
        } catch (e: Exception) {
            NetworkResult.UnknownError(e)
        }
    }
}
