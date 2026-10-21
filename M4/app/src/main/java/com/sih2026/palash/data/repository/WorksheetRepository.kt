package com.sih2026.palash.data.repository

import com.google.gson.JsonObject
import com.sih2026.palash.data.local.dao.WorksheetDao
import com.sih2026.palash.data.local.entity.WorksheetEntity
import com.sih2026.palash.data.network.ConnectivityObserver
import com.sih2026.palash.data.network.NetworkClient
import com.sih2026.palash.data.network.NetworkConfig
import com.sih2026.palash.data.network.NetworkConnectivityState
import com.sih2026.palash.data.network.NetworkErrorMessage
import com.sih2026.palash.data.network.NetworkResult
import com.sih2026.palash.data.network.dto.WorksheetDto
import kotlinx.coroutines.flow.Flow

interface WorksheetRepository {
    fun getAllWorksheets(): Flow<List<WorksheetEntity>>
    fun getWorksheetsByTopic(grade: String, subject: String, topic: String): Flow<List<WorksheetEntity>>
    suspend fun getWorksheetById(id: String): WorksheetEntity?
    suspend fun insertWorksheet(worksheet: WorksheetEntity)
    suspend fun insertWorksheets(worksheets: List<WorksheetEntity>)
    suspend fun syncWorksheetsFromBackend(lessonId: Int? = null): NetworkResult<List<WorksheetEntity>>
}

/**
 * Validates backend WorksheetDto according to M1 data validation rules.
 */
fun WorksheetDto.isValid(): Boolean {
    return id > 0 &&
            lessonId > 0 &&
            title.isNotBlank() &&
            content.isNotBlank() &&
            language.isNotBlank() &&
            version > 0
}

/**
 * Safely maps a validated WorksheetDto to Room WorksheetEntity.
 */
fun WorksheetDto.toEntity(): WorksheetEntity {
    val jsonObject = JsonObject().apply {
        addProperty("content", content)
        addProperty("lesson_id", lessonId)
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

    return WorksheetEntity(
        id = "backend_worksheet_$id",
        grade = "Grade 2",
        subject = "Mathematics",
        topic = title,
        title = title,
        sourceLanguage = "hi",
        targetLanguage = "sat",
        contentJson = jsonObject.toString(),
        version = version.toString(),
        createdAt = parsedCreatedAtMs
    )
}

class WorksheetRepositoryImpl(
    private val worksheetDao: WorksheetDao,
    private val connectivityObserver: ConnectivityObserver? = null
) : WorksheetRepository {

    override fun getAllWorksheets(): Flow<List<WorksheetEntity>> = worksheetDao.getAllWorksheets()

    override fun getWorksheetsByTopic(grade: String, subject: String, topic: String): Flow<List<WorksheetEntity>> =
        worksheetDao.getWorksheetsByTopic(grade, subject, topic)

    override suspend fun getWorksheetById(id: String): WorksheetEntity? = worksheetDao.getWorksheetById(id)

    override suspend fun insertWorksheet(worksheet: WorksheetEntity) = worksheetDao.insertWorksheet(worksheet)

    override suspend fun insertWorksheets(worksheets: List<WorksheetEntity>) = worksheetDao.insertWorksheets(worksheets)

    override suspend fun syncWorksheetsFromBackend(lessonId: Int?): NetworkResult<List<WorksheetEntity>> {
        if (!NetworkConfig.isConfigured) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)
        }

        if (connectivityObserver != null && connectivityObserver.getCurrentState() == NetworkConnectivityState.OFFLINE) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.INTERNET_UNAVAILABLE)
        }

        val apiService = NetworkClient.getApiService()
            ?: return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)

        return try {
            val response = apiService.getWorksheets(lessonId = lessonId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.worksheets.isNotEmpty()) {
                    val validEntities = body.worksheets
                        .filter { it.isValid() }
                        .map { it.toEntity() }

                    if (validEntities.isNotEmpty()) {
                        worksheetDao.insertWorksheets(validEntities)
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
