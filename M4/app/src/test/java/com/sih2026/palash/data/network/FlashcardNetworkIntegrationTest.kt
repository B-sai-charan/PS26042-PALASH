package com.sih2026.palash.data.network

import com.google.gson.Gson
import com.sih2026.palash.data.local.dao.FlashcardDao
import com.sih2026.palash.data.local.entity.FlashcardEntity
import com.sih2026.palash.data.network.dto.FlashcardDto
import com.sih2026.palash.data.network.dto.FlashcardsResponseDto
import com.sih2026.palash.data.repository.FlashcardRepositoryImpl
import com.sih2026.palash.data.repository.isValid
import com.sih2026.palash.data.repository.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying GET /flashcards endpoint, DTO mapping, validation, and Room fallback.
 */
class FlashcardNetworkIntegrationTest {

    @Before
    fun setUp() {
        NetworkConfig.reset()
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    fun test1_flashcardDtoMapping() {
        val json = """
            {
              "id": 1,
              "lesson_id": 1,
              "front_text": "1",
              "back_text": "᱑",
              "language": "hin_Deva-sat_Olck",
              "version": 1,
              "created_at": "2026-09-06T10:00:00Z",
              "updated_at": "2026-09-06T10:00:00Z"
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, FlashcardDto::class.java)
        assertEquals(1, dto.id)
        assertEquals(1, dto.lessonId)
        assertEquals("1", dto.frontText)
        assertEquals("᱑", dto.backText)
        assertEquals("hin_Deva-sat_Olck", dto.language)
        assertEquals(1, dto.version)
    }

    @Test
    fun test2_flashcardsResponseDtoMapping() {
        val json = """
            {
              "status": "success",
              "count": 10,
              "flashcards": [
                {
                  "id": 1,
                  "lesson_id": 1,
                  "front_text": "1",
                  "back_text": "᱑",
                  "language": "hin_Deva-sat_Olck",
                  "version": 1,
                  "created_at": "2026-09-06T10:00:00Z",
                  "updated_at": "2026-09-06T10:00:00Z"
                }
              ]
            }
        """.trimIndent()

        val response = Gson().fromJson(json, FlashcardsResponseDto::class.java)
        assertEquals("success", response.status)
        assertEquals(10, response.count)
        assertEquals(1, response.flashcards.size)
        assertEquals("1", response.flashcards[0].frontText)
        assertEquals("᱑", response.flashcards[0].backText)
    }

    @Test
    fun test3_validFlashcardValidation() {
        val dto = FlashcardDto(
            id = 1,
            lessonId = 1,
            frontText = "1",
            backText = "᱑",
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = null,
            updatedAt = null
        )

        assertTrue(dto.isValid())
    }

    @Test
    fun test4_invalidFlashcardRejection() {
        val invalidDto1 = FlashcardDto(
            id = 0,
            lessonId = 1,
            frontText = "1",
            backText = "᱑",
            language = "lang",
            version = 1,
            createdAt = null,
            updatedAt = null
        )
        val invalidDto2 = FlashcardDto(
            id = 1,
            lessonId = 0,
            frontText = "",
            backText = "᱑",
            language = "lang",
            version = 1,
            createdAt = null,
            updatedAt = null
        )
        val invalidDto3 = FlashcardDto(
            id = 1,
            lessonId = 1,
            frontText = "1",
            backText = " ",
            language = "",
            version = 0,
            createdAt = null,
            updatedAt = null
        )

        assertFalse(invalidDto1.isValid())
        assertFalse(invalidDto2.isValid())
        assertFalse(invalidDto3.isValid())
    }

    @Test
    fun test5_dtoToFlashcardEntityMapping() {
        val dto = FlashcardDto(
            id = 1,
            lessonId = 1,
            frontText = "1",
            backText = "᱑",
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = "2026-09-06T10:00:00Z",
            updatedAt = "2026-09-06T10:00:00Z"
        )

        val entity = dto.toEntity()
        assertEquals("backend_flashcard_1", entity.id)
        assertEquals(1, entity.lessonId)
        assertEquals("1", entity.frontText)
        assertEquals("᱑", entity.backText)
        assertEquals("hin_Deva-sat_Olck", entity.language)
        assertEquals(1, entity.version)
    }

    @Test
    fun test6_offlineRoomFallback() = runBlocking {
        val fakeDao = FakeFlashcardDao()
        fakeDao.insertFlashcards(
            listOf(
                FlashcardEntity(
                    id = "local_flashcard_1",
                    lessonId = 1,
                    frontText = "2",
                    backText = "᱒",
                    language = "hin_Deva-sat_Olck",
                    version = 1
                )
            )
        )

        val repository = FlashcardRepositoryImpl(fakeDao)
        val result = repository.syncFlashcardsFromBackend(1)

        assertTrue(result is NetworkResult.NetworkUnavailable)

        var localCards: List<FlashcardEntity> = emptyList()
        repository.getFlashcardsByLessonId(1).collect { localCards = it }
        assertEquals(1, localCards.size)
        assertEquals("2", localCards[0].frontText)
        assertEquals("᱒", localCards[0].backText)
    }

    @Test
    fun test7_emptyStateWhenNoBackendAndNoCache() = runBlocking {
        val fakeDao = FakeFlashcardDao()
        val repository = FlashcardRepositoryImpl(fakeDao)
        val syncResult = repository.syncFlashcardsFromBackend(1)

        assertTrue(syncResult is NetworkResult.NetworkUnavailable)

        var localCards: List<FlashcardEntity> = emptyList()
        repository.getAllFlashcards().collect { localCards = it }
        assertTrue(localCards.isEmpty())
    }

    private class FakeFlashcardDao : FlashcardDao {
        private val map = mutableMapOf<String, FlashcardEntity>()

        override fun getAllFlashcards(): Flow<List<FlashcardEntity>> = flowOf(map.values.toList())

        override fun getFlashcardsByLessonId(lessonId: Int): Flow<List<FlashcardEntity>> =
            flowOf(map.values.filter { it.lessonId == lessonId })

        override suspend fun insertFlashcards(flashcards: List<FlashcardEntity>) {
            flashcards.forEach { map[it.id] = it }
        }

        override suspend fun clearFlashcards() {
            map.clear()
        }
    }
}
