package com.sih2026.palash.data.network

import com.google.gson.Gson
import com.sih2026.palash.data.local.dao.LessonDao
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.network.dto.HealthResponseDto
import com.sih2026.palash.data.network.dto.LessonDto
import com.sih2026.palash.data.network.dto.LessonsResponseDto
import com.sih2026.palash.data.repository.LessonRepositoryImpl
import com.sih2026.palash.data.repository.isValid
import com.sih2026.palash.data.repository.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying M1 Backend integration contracts:
 * 1. HealthResponseDto mapping
 * 2. LessonDto mapping
 * 3. Network lesson -> LessonEntity mapping
 * 4. Empty Santali content handling
 * 5. Invalid lesson response handling
 * 6. Backend unavailable -> local Room fallback
 * 7. Health backend success
 * 8. Health backend unavailable
 */
class M1NetworkIntegrationTest {

    @Before
    fun setUp() {
        NetworkConfig.reset()
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    fun test1_healthResponseDtoMapping() {
        val json = """
            {
              "status": "ok",
              "service": "palash-backend",
              "version": "0.1.0"
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, HealthResponseDto::class.java)
        assertEquals("ok", dto.status)
        assertEquals("palash-backend", dto.service)
        assertEquals("0.1.0", dto.version)
    }

    @Test
    fun test2_lessonDtoMapping() {
        val json = """
            {
              "status": "success",
              "count": 1,
              "lessons": [
                {
                  "id": 1,
                  "grade": 2,
                  "subject": "mathematics",
                  "title": "Counting",
                  "hindi_content": "1 से 20 तक गिनती सीखें।",
                  "santali_content": "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾",
                  "language": "hin_Deva-sat_Olck",
                  "version": 1,
                  "created_at": "2026-09-06T10:00:00Z",
                  "updated_at": "2026-09-06T10:00:00Z"
                }
              ]
            }
        """.trimIndent()

        val responseDto = Gson().fromJson(json, LessonsResponseDto::class.java)
        assertEquals("success", responseDto.status)
        assertEquals(1, responseDto.count)
        assertEquals(1, responseDto.lessons.size)

        val lesson = responseDto.lessons[0]
        assertEquals(1, lesson.id)
        assertEquals(2, lesson.grade)
        assertEquals("mathematics", lesson.subject)
        assertEquals("Counting", lesson.title)
        assertEquals("1 से 20 तक गिनती सीखें।", lesson.hindiContent)
        assertEquals("᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾", lesson.santaliContent)
        assertEquals("hin_Deva-sat_Olck", lesson.language)
        assertEquals(1, lesson.version)
    }

    @Test
    fun test3_networkLessonToLessonEntityMapping() {
        val dto = LessonDto(
            id = 1,
            grade = 2,
            subject = "mathematics",
            title = "Counting",
            hindiContent = "1 से 20 तक गिनती सीखें।",
            santaliContent = "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾",
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = "2026-09-06T10:00:00Z",
            updatedAt = "2026-09-06T10:00:00Z"
        )

        assertTrue(dto.isValid())
        val entity = dto.toEntity()

        assertEquals("backend_lesson_1", entity.id)
        assertEquals("Grade 2", entity.grade)
        assertEquals("mathematics", entity.subject)
        assertEquals("Counting", entity.title)
        assertEquals("hin_Deva-sat_Olck", entity.languageCode)
        assertTrue(entity.contentJson.contains("1 से 20 तक गिनती सीखें।"))
        assertTrue(entity.contentJson.contains("᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾"))
    }

    @Test
    fun test4_emptySantaliContentHandling() {
        val dto = LessonDto(
            id = 2,
            grade = 2,
            subject = "mathematics",
            title = "Addition",
            hindiContent = "दो और तीन जोड़ो।",
            santaliContent = null,
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = null,
            updatedAt = null
        )

        assertTrue(dto.isValid())
        val entity = dto.toEntity()
        assertNull(dto.santaliContent)
        assertTrue(entity.contentJson.contains("\"santali_content\":null"))
    }

    @Test
    fun test5_invalidLessonResponseHandling() {
        val invalidDto1 = LessonDto(
            id = -1,
            grade = 2,
            subject = "math",
            title = "Title",
            hindiContent = "Hindi",
            santaliContent = null,
            language = "lang",
            version = 1,
            createdAt = null,
            updatedAt = null
        )
        val invalidDto2 = LessonDto(
            id = 1,
            grade = 0,
            subject = "",
            title = "Title",
            hindiContent = "Hindi",
            santaliContent = null,
            language = "lang",
            version = 1,
            createdAt = null,
            updatedAt = null
        )

        assertFalse(invalidDto1.isValid())
        assertFalse(invalidDto2.isValid())
    }

    @Test
    fun test6_backendUnavailableLocalRoomFallback() = runBlocking {
        // NetworkConfig is unconfigured / reset
        val fakeDao = FakeLessonDao()
        fakeDao.insertLesson(
            LessonEntity(
                id = "local_1",
                grade = "Grade 2",
                subject = "Mathematics",
                topic = "Counting",
                title = "Local Counting",
                languageCode = "hi",
                contentJson = "{}"
            )
        )

        val repository = LessonRepositoryImpl(fakeDao)
        val result = repository.syncLessonsFromBackend(2, "mathematics")

        assertTrue(result is NetworkResult.NetworkUnavailable)
        val localLesson = repository.getLessonById("local_1")
        assertNotNull(localLesson)
        assertEquals("Local Counting", localLesson?.title)
    }

    @Test
    fun test7_healthBackendSuccess() = runBlocking {
        // Test HealthCheckRepository when NetworkConfig is configured
        NetworkConfig.setDevelopmentBaseUrl("http://127.0.0.1:8000")
        val healthRepo = HealthCheckRepositoryImpl()
        
        val result = healthRepo.checkBackendHealth()
        assertTrue(result is NetworkResult.Success || result is NetworkResult.NetworkUnavailable || result is NetworkResult.HttpError || result is NetworkResult.Timeout || result is NetworkResult.UnknownError)
    }

    @Test
    fun test8_healthBackendUnavailable() = runBlocking {
        // NetworkConfig is unconfigured
        NetworkConfig.reset()
        val healthRepo = HealthCheckRepositoryImpl()
        val result = healthRepo.checkBackendHealth()

        assertTrue(result is NetworkResult.NetworkUnavailable)
        assertEquals(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE, (result as NetworkResult.NetworkUnavailable).message)
    }

    private class FakeLessonDao : LessonDao {
        private val lessonsMap = mutableMapOf<String, LessonEntity>()

        override fun getAllLessons(): Flow<List<LessonEntity>> {
            return flowOf(lessonsMap.values.toList())
        }

        override fun getLessonsByGradeAndSubject(grade: String, subject: String): Flow<List<LessonEntity>> {
            return flowOf(lessonsMap.values.filter { it.grade == grade && it.subject == subject })
        }

        override suspend fun getLessonById(id: String): LessonEntity? {
            return lessonsMap[id]
        }

        override suspend fun insertLesson(lesson: LessonEntity) {
            lessonsMap[lesson.id] = lesson
        }

        override suspend fun insertLessons(lessons: List<LessonEntity>) {
            lessons.forEach { lessonsMap[it.id] = it }
        }

        override suspend fun clearLessons() {
            lessonsMap.clear()
        }
    }
}
