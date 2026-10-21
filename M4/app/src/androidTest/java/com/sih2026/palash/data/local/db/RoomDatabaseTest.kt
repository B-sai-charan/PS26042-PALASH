package com.sih2026.palash.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.local.entity.WorksheetEntity
import com.sih2026.palash.data.local.seeder.DatabaseSeeder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {
    private lateinit var database: PalashDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, PalashDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetLesson() = runBlocking {
        val lesson = LessonEntity(
            id = "test_lesson_1",
            grade = "Grade 2",
            subject = "Mathematics",
            topic = "Counting",
            title = "Counting Test",
            languageCode = "hi",
            contentJson = "{}"
        )
        database.lessonDao().insertLesson(lesson)

        val retrieved = database.lessonDao().getLessonById("test_lesson_1")
        assertNotNull(retrieved)
        assertEquals("Grade 2", retrieved?.grade)
        assertEquals("Mathematics", retrieved?.subject)
    }

    @Test
    fun insertAndGetTranslationCache() = runBlocking {
        val cache = TranslationCacheEntity(
            id = "test_trans_1",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "पाँच आम गिनो।",
            targetText = null,
            validationStatus = com.sih2026.palash.translation.ValidationStatus.UNVERIFIED.name
        )
        database.translationCacheDao().insertTranslation(cache)

        val retrieved = database.translationCacheDao().findTranslation("पाँच आम गिनो।", "hi", "sat")
        assertNotNull(retrieved)
        org.junit.Assert.assertNull(retrieved?.targetText)
    }

    @Test
    fun insertAndGetWorksheet() = runBlocking {
        val worksheet = WorksheetEntity(
            id = "test_worksheet_1",
            grade = "Grade 2",
            subject = "Mathematics",
            topic = "Counting",
            title = "Visual Counting Worksheet",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            contentJson = "{}",
            version = "1.0"
        )
        database.worksheetDao().insertWorksheet(worksheet)

        val retrieved = database.worksheetDao().getWorksheetById("test_worksheet_1")
        assertNotNull(retrieved)
        assertEquals("Visual Counting Worksheet", retrieved?.title)
    }

    @Test
    fun insertAndGetLanguagePackMetadata() = runBlocking {
        val pack = LanguagePackMetadataEntity(
            languageCode = "sat",
            languageName = "Santali",
            version = "0.1-demo",
            status = "INSTALLED",
            checksum = "test-hash"
        )
        database.languagePackMetadataDao().insertLanguagePack(pack)

        val retrieved = database.languagePackMetadataDao().getLanguagePack("sat")
        assertNotNull(retrieved)
        assertEquals("Santali", retrieved?.languageName)
    }

    @Test
    fun testDatabaseSeeding() = runBlocking {
        DatabaseSeeder.seedDatabase(database)

        val lesson = database.lessonDao().getLessonById("lesson_g2_math_counting")
        assertNotNull(lesson)
        assertEquals("Grade 2", lesson?.grade)

        val translation = database.translationCacheDao().findTranslation("पाँच आम गिनो।", "hi", "sat")
        assertNotNull(translation)

        val pack = database.languagePackMetadataDao().getLanguagePack("sat")
        assertNotNull(pack)
        assertEquals("Santali", pack?.languageName)
    }
}
