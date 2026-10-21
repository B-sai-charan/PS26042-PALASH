package com.sih2026.palash.data.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.repository.FlashcardRepositoryImpl
import com.sih2026.palash.data.repository.LessonRepositoryImpl
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import com.sih2026.palash.data.repository.WorksheetRepositoryImpl
import com.sih2026.palash.translation.ValidationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented physical-device integration test.
 * Executes on real physical device (iQOO Neo9 Pro) connected via ADB reverse tcp:8000 tcp:8000.
 */
@RunWith(AndroidJUnit4::class)
class PhysicalM1IntegrationTest {

    @Before
    fun setUp() {
        NetworkConfig.setDevelopmentBaseUrl("http://127.0.0.1:8000")
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    fun testPhysicalAndroid_healthCheck() = runBlocking {
        val repository = HealthCheckRepositoryImpl()
        val result = repository.checkBackendHealth()

        assertTrue(
            "Expected NetworkResult.Success from physical Android device to http://127.0.0.1:8000/health, but got: $result",
            result is NetworkResult.Success
        )
    }

    @Test
    fun testPhysicalAndroid_lessonsSyncAndRoomStorage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = DatabaseProvider.getDatabase(context)
        val lessonDao = database.lessonDao()

        val repository = LessonRepositoryImpl(lessonDao)
        val syncResult = repository.syncLessonsFromBackend(grade = 2, subject = "mathematics")

        assertTrue(
            "Expected NetworkResult.Success from physical Android device to http://127.0.0.1:8000/lessons, but got: $syncResult",
            syncResult is NetworkResult.Success
        )

        val lessons = (syncResult as NetworkResult.Success).data
        assertTrue(
            "Expected at least 1 lesson retrieved from backend, got size: ${lessons.size}",
            lessons.isNotEmpty()
        )

        val firstLesson = lessons[0]
        assertEquals("Grade 2", firstLesson.grade)
        assertEquals("mathematics", firstLesson.subject)

        // Verify Room persistence
        val roomLesson = lessonDao.getLessonById(firstLesson.id)
        assertNotNull("Expected lesson to be persisted in Room database", roomLesson)
        assertEquals(firstLesson.title, roomLesson?.title)

        // Test Offline Fallback: Reset network configuration to simulate offline state
        NetworkConfig.reset()
        val offlineResult = repository.syncLessonsFromBackend(grade = 2, subject = "mathematics")
        assertTrue("Expected NetworkUnavailable when offline", offlineResult is NetworkResult.NetworkUnavailable)

        // Local Room content is still accessible offline
        val cachedRoomLesson = lessonDao.getLessonById(firstLesson.id)
        assertNotNull("Expected cached lesson to remain available offline in Room", cachedRoomLesson)
        assertEquals(firstLesson.title, cachedRoomLesson?.title)
    }

    @Test
    fun testPhysicalAndroid_worksheetsSyncAndRoomStorage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = DatabaseProvider.getDatabase(context)
        val worksheetDao = database.worksheetDao()

        val repository = WorksheetRepositoryImpl(worksheetDao)
        val syncResult = repository.syncWorksheetsFromBackend(lessonId = 1)

        assertTrue(
            "Expected NetworkResult.Success from physical Android device to http://127.0.0.1:8000/worksheets?lesson_id=1, but got: $syncResult",
            syncResult is NetworkResult.Success
        )

        val worksheets = (syncResult as NetworkResult.Success).data
        assertTrue(
            "Expected at least 1 worksheet retrieved from backend, got size: ${worksheets.size}",
            worksheets.isNotEmpty()
        )

        val firstWorksheet = worksheets[0]
        assertEquals("Counting 1 to 20", firstWorksheet.title)

        // Verify Room persistence
        val roomWorksheet = worksheetDao.getWorksheetById(firstWorksheet.id)
        assertNotNull("Expected worksheet to be persisted in Room database", roomWorksheet)
        assertEquals(firstWorksheet.title, roomWorksheet?.title)

        // Test Offline Fallback
        NetworkConfig.reset()
        val offlineResult = repository.syncWorksheetsFromBackend(lessonId = 1)
        assertTrue("Expected NetworkUnavailable when offline", offlineResult is NetworkResult.NetworkUnavailable)

        // Local Room content is still accessible offline
        val cachedRoomWorksheet = worksheetDao.getWorksheetById(firstWorksheet.id)
        assertNotNull("Expected cached worksheet to remain available offline in Room", cachedRoomWorksheet)
        assertEquals(firstWorksheet.title, cachedRoomWorksheet?.title)
    }

    @Test
    fun testPhysicalAndroid_flashcardsSyncAndRoomStorage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = DatabaseProvider.getDatabase(context)
        val flashcardDao = database.flashcardDao()

        val repository = FlashcardRepositoryImpl(flashcardDao)
        val syncResult = repository.syncFlashcardsFromBackend(lessonId = 1)

        assertTrue(
            "Expected NetworkResult.Success from physical Android device to http://127.0.0.1:8000/flashcards?lesson_id=1, but got: $syncResult",
            syncResult is NetworkResult.Success
        )

        val flashcards = (syncResult as NetworkResult.Success).data
        assertTrue(
            "Expected at least 1 flashcard retrieved from backend, got size: ${flashcards.size}",
            flashcards.isNotEmpty()
        )

        val firstFlashcard = flashcards[0]
        assertEquals("1", firstFlashcard.frontText)
        assertEquals("᱑", firstFlashcard.backText)

        // Verify Room persistence
        val roomFlashcards = flashcardDao.getFlashcardsByLessonId(1).first()
        assertTrue("Expected flashcard to be persisted in Room database", roomFlashcards.isNotEmpty())
        assertEquals(firstFlashcard.frontText, roomFlashcards[0].frontText)

        // Test Offline Fallback
        NetworkConfig.reset()
        val offlineResult = repository.syncFlashcardsFromBackend(lessonId = 1)
        assertTrue("Expected NetworkUnavailable when offline", offlineResult is NetworkResult.NetworkUnavailable)

        // Local Room content is still accessible offline
        val cachedFlashcards = flashcardDao.getFlashcardsByLessonId(1).first()
        assertTrue("Expected cached flashcards to remain available offline in Room", cachedFlashcards.isNotEmpty())
        assertEquals(firstFlashcard.frontText, cachedFlashcards[0].frontText)
    }

    @Test
    fun testPhysicalAndroid_translateSyncAndRoomStorage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = DatabaseProvider.getDatabase(context)
        val translationDao = database.translationCacheDao()
        val repository = TranslationRepositoryImpl(translationDao)

        val hindiPhrase = "किसानों को सरकारी योजना का लाभ मिलेगा।"
        val syncResult = repository.translateHindiToSantali(hindiPhrase)

        assertTrue(
            "Expected NetworkResult.Success from physical Android device to http://127.0.0.1:8000/translate, but got: $syncResult",
            syncResult is NetworkResult.Success
        )

        val data = (syncResult as NetworkResult.Success).data
        assertFalse("Real backend result must NOT be marked as from cache", data.isFromCache)
        assertEquals("hin_Deva", data.dto.sourceLanguage)
        assertEquals("sat_Olck", data.dto.targetLanguage)
        assertEquals("success", data.dto.status)
        assertTrue("Expected non-blank translated_text", data.dto.translatedText.isNotBlank())
        assertTrue("Expected latency_ms >= 0", data.dto.latencyMs >= 0.0)

        // Verify Room Persistence: Check stored entity
        val roomRecord = translationDao.findTranslation(hindiPhrase, "hin_Deva", "sat_Olck")
        assertNotNull("Expected translation to be saved in Room database", roomRecord)
        assertEquals(data.dto.translatedText, roomRecord?.targetText)
        assertEquals(ValidationStatus.UNVERIFIED.name, roomRecord?.validationStatus)

        // Test Offline Fallback: Reset network configuration to simulate offline state
        NetworkConfig.reset()
        val offlineHitResult = repository.translateHindiToSantali(hindiPhrase)
        assertTrue(
            "Expected offline cache hit to return NetworkResult.Success",
            offlineHitResult is NetworkResult.Success
        )
        val offlineData = (offlineHitResult as NetworkResult.Success).data
        assertTrue("Offline hit must be marked as from cache", offlineData.isFromCache)
        assertEquals(data.dto.translatedText, offlineData.dto.translatedText)

        // Test Offline Cache Miss: Uncached phrase must return unavailable and NOT fabricate translation
        val uncachedPhrase = "अनजान पाठ्य वाक्य"
        val offlineMissResult = repository.translateHindiToSantali(uncachedPhrase)
        assertTrue(
            "Expected offline cache miss to return NetworkUnavailable",
            offlineMissResult is NetworkResult.NetworkUnavailable
        )
        val missingRoomRecord = translationDao.findTranslation(uncachedPhrase, "hin_Deva", "sat_Olck")
        assertNull("Uncached phrase must NOT be stored or fabricated in Room", missingRoomRecord)
    }
}
