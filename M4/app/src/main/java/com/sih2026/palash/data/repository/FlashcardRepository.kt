package com.sih2026.palash.data.repository

import com.sih2026.palash.data.local.dao.FlashcardDao
import com.sih2026.palash.data.local.entity.FlashcardEntity
import com.sih2026.palash.data.network.ConnectivityObserver
import com.sih2026.palash.data.network.NetworkClient
import com.sih2026.palash.data.network.NetworkConfig
import com.sih2026.palash.data.network.NetworkConnectivityState
import com.sih2026.palash.data.network.NetworkErrorMessage
import com.sih2026.palash.data.network.NetworkResult
import com.sih2026.palash.data.network.dto.FlashcardDto
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>
    fun getFlashcardsByLessonId(lessonId: Int): Flow<List<FlashcardEntity>>
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)
    suspend fun syncFlashcardsFromBackend(lessonId: Int? = null): NetworkResult<List<FlashcardEntity>>
}

/**
 * Validates backend FlashcardDto according to M1 data validation rules.
 */
fun FlashcardDto.isValid(): Boolean {
    return id > 0 &&
            lessonId > 0 &&
            frontText.isNotBlank() &&
            backText.isNotBlank() &&
            language.isNotBlank() &&
            version > 0
}

/**
 * Safely maps a validated FlashcardDto to Room FlashcardEntity.
 */
fun FlashcardDto.toEntity(): FlashcardEntity {
    val parsedCreatedAtMs = try {
        if (!createdAt.isNullOrBlank()) {
            java.time.Instant.parse(createdAt).toEpochMilli()
        } else {
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return FlashcardEntity(
        id = "backend_flashcard_$id",
        lessonId = lessonId,
        frontText = frontText,
        backText = backText,
        language = language,
        version = version,
        createdAt = parsedCreatedAtMs
    )
}

class FlashcardRepositoryImpl(
    private val flashcardDao: FlashcardDao,
    private val connectivityObserver: ConnectivityObserver? = null
) : FlashcardRepository {

    override fun getAllFlashcards(): Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcards()

    override fun getFlashcardsByLessonId(lessonId: Int): Flow<List<FlashcardEntity>> =
        flashcardDao.getFlashcardsByLessonId(lessonId)

    override suspend fun insertFlashcards(flashcards: List<FlashcardEntity>) =
        flashcardDao.insertFlashcards(flashcards)

    override suspend fun syncFlashcardsFromBackend(lessonId: Int?): NetworkResult<List<FlashcardEntity>> {
        if (!NetworkConfig.isConfigured) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)
        }

        if (connectivityObserver != null && connectivityObserver.getCurrentState() == NetworkConnectivityState.OFFLINE) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.INTERNET_UNAVAILABLE)
        }

        val apiService = NetworkClient.getApiService()
            ?: return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)

        return try {
            val response = apiService.getFlashcards(lessonId = lessonId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.flashcards.isNotEmpty()) {
                    val validEntities = body.flashcards
                        .filter { it.isValid() }
                        .map { it.toEntity() }

                    if (validEntities.isNotEmpty()) {
                        flashcardDao.insertFlashcards(validEntities)
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
