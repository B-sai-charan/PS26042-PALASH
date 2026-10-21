package com.sih2026.palash.data.network

import com.google.gson.Gson
import com.sih2026.palash.data.local.dao.WorksheetDao
import com.sih2026.palash.data.local.entity.WorksheetEntity
import com.sih2026.palash.data.network.dto.WorksheetDto
import com.sih2026.palash.data.network.dto.WorksheetsResponseDto
import com.sih2026.palash.data.repository.WorksheetRepositoryImpl
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
 * Unit tests verifying GET /worksheets endpoint, DTO mapping, validation, and Room fallback.
 */
class WorksheetNetworkIntegrationTest {

    @Before
    fun setUp() {
        NetworkConfig.reset()
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    fun test1_worksheetDtoMapping() {
        val json = """
            {
              "id": 1,
              "lesson_id": 1,
              "title": "Counting 1 to 20",
              "content": "Worksheet content text block\n1. 1, 2, 3\nSantali:\n᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾",
              "language": "hin_Deva-sat_Olck",
              "version": 1,
              "created_at": "2026-09-06T10:00:00Z",
              "updated_at": "2026-09-06T10:00:00Z"
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, WorksheetDto::class.java)
        assertEquals(1, dto.id)
        assertEquals(1, dto.lessonId)
        assertEquals("Counting 1 to 20", dto.title)
        assertTrue(dto.content.contains("1. 1, 2, 3"))
        assertTrue(dto.content.contains("᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾"))
        assertEquals("hin_Deva-sat_Olck", dto.language)
        assertEquals(1, dto.version)
    }

    @Test
    fun test2_worksheetsResponseDtoMapping() {
        val json = """
            {
              "status": "success",
              "count": 1,
              "worksheets": [
                {
                  "id": 1,
                  "lesson_id": 1,
                  "title": "Counting 1 to 20",
                  "content": "Sample content",
                  "language": "hin_Deva-sat_Olck",
                  "version": 1,
                  "created_at": "2026-09-06T10:00:00Z",
                  "updated_at": "2026-09-06T10:00:00Z"
                }
              ]
            }
        """.trimIndent()

        val response = Gson().fromJson(json, WorksheetsResponseDto::class.java)
        assertEquals("success", response.status)
        assertEquals(1, response.count)
        assertEquals(1, response.worksheets.size)
        assertEquals(1, response.worksheets[0].id)
    }

    @Test
    fun test3_validWorksheetValidation() {
        val dto = WorksheetDto(
            id = 1,
            lessonId = 1,
            title = "Counting 1 to 20",
            content = "Valid Content",
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = null,
            updatedAt = null
        )

        assertTrue(dto.isValid())
    }

    @Test
    fun test4_invalidWorksheetRejection() {
        val invalidDto1 = WorksheetDto(
            id = 0,
            lessonId = 1,
            title = "Title",
            content = "Content",
            language = "lang",
            version = 1,
            createdAt = null,
            updatedAt = null
        )
        val invalidDto2 = WorksheetDto(
            id = 1,
            lessonId = 0,
            title = "",
            content = "Content",
            language = "lang",
            version = 1,
            createdAt = null,
            updatedAt = null
        )
        val invalidDto3 = WorksheetDto(
            id = 1,
            lessonId = 1,
            title = "Title",
            content = "  ",
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
    fun test5_dtoToWorksheetEntityMapping() {
        val dto = WorksheetDto(
            id = 1,
            lessonId = 1,
            title = "Counting 1 to 20",
            content = "Worksheet content text block",
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = "2026-09-06T10:00:00Z",
            updatedAt = "2026-09-06T10:00:00Z"
        )

        val entity = dto.toEntity()
        assertEquals("backend_worksheet_1", entity.id)
        assertEquals("Grade 2", entity.grade)
        assertEquals("Mathematics", entity.subject)
        assertEquals("Counting 1 to 20", entity.title)
        assertEquals("hi", entity.sourceLanguage)
        assertEquals("sat", entity.targetLanguage)
        assertTrue(entity.contentJson.contains("Worksheet content text block"))
    }

    @Test
    fun test6_networkFailureRoomFallback() = runBlocking {
        // Unconfigured network
        val fakeDao = FakeWorksheetDao()
        fakeDao.insertWorksheet(
            WorksheetEntity(
                id = "local_worksheet_1",
                grade = "Grade 2",
                subject = "Mathematics",
                topic = "Counting",
                title = "Local Worksheet",
                sourceLanguage = "hi",
                targetLanguage = "sat",
                contentJson = """{"content":"Local worksheet content"}""",
                version = "1"
            )
        )

        val repository = WorksheetRepositoryImpl(fakeDao)
        val result = repository.syncWorksheetsFromBackend(1)

        assertTrue(result is NetworkResult.NetworkUnavailable)
        val cached = repository.getWorksheetById("local_worksheet_1")
        assertNotNull(cached)
        assertEquals("Local Worksheet", cached?.title)
    }

    @Test
    fun test7_emptySantaliContentHandlingWithoutMutation() {
        val rawSantaliContent = "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ᱾"
        val dto = WorksheetDto(
            id = 1,
            lessonId = 1,
            title = "Counting",
            content = rawSantaliContent,
            language = "hin_Deva-sat_Olck",
            version = 1,
            createdAt = null,
            updatedAt = null
        )

        val entity = dto.toEntity()
        assertTrue(entity.contentJson.contains(rawSantaliContent))
    }

    private class FakeWorksheetDao : WorksheetDao {
        private val map = mutableMapOf<String, WorksheetEntity>()

        override fun getAllWorksheets(): Flow<List<WorksheetEntity>> = flowOf(map.values.toList())

        override fun getWorksheetsByTopic(grade: String, subject: String, topic: String): Flow<List<WorksheetEntity>> =
            flowOf(map.values.filter { it.grade == grade && it.subject == subject && it.topic == topic })

        override suspend fun getWorksheetById(id: String): WorksheetEntity? = map[id]

        override suspend fun insertWorksheet(worksheet: WorksheetEntity) {
            map[worksheet.id] = worksheet
        }

        override suspend fun insertWorksheets(worksheets: List<WorksheetEntity>) {
            worksheets.forEach { map[it.id] = it }
        }

        override suspend fun clearWorksheets() {
            map.clear()
        }
    }
}
