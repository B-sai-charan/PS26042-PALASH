package com.sih2026.palash.data.local.pack

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sih2026.palash.data.local.db.PalashDatabase
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LanguagePackStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentPackManagerRoomTest {
    private lateinit var database: PalashDatabase
    private lateinit var manager: ContentPackManager

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, PalashDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        manager = ContentPackManager(database)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testAtomicInstallationSuccess() = runBlocking {
        val demoPack = DemoContentPackFactory.createSantaliDemoPack()
        val success = manager.installPackAtomically(demoPack)

        assertTrue(success)
        assertTrue(manager.isPackInstalled("sat"))
        assertEquals("0.1-demo", manager.getInstalledVersion("sat"))

        val lesson = database.lessonDao().getLessonById("lesson_g2_math_counting")
        assertNotNull(lesson)

        val worksheet = database.worksheetDao().getWorksheetById("q1")
        assertNotNull(worksheet)
    }

    @Test
    fun testAtomicInstallationFailureRollback() = runBlocking {
        // Create an invalid pack with empty checksum
        val invalidMetadata = LanguagePackMetadataEntity(
            languageCode = "sat",
            languageName = "Santali",
            script = "Ol Chiki",
            version = "0.1-invalid",
            status = LanguagePackStatus.INSTALLED.name,
            checksum = "" // Empty checksum triggers validation failure
        )
        val invalidPack = ContentPack(
            metadata = invalidMetadata,
            lessons = emptyList(),
            worksheets = emptyList(),
            translations = emptyList()
        )

        val success = manager.installPackAtomically(invalidPack)
        assertFalse(success)
        assertFalse(manager.isPackInstalled("sat"))
    }

    @Test
    fun testResetDemoPack() = runBlocking {
        val success = manager.resetDemoPack()
        assertTrue(success)
        assertTrue(manager.isPackInstalled("sat"))
        assertEquals(DemoContentPackFactory.SANTALI_VERSION, manager.getInstalledVersion("sat"))
    }

    @Test
    fun testRemovePackMetadata() = runBlocking {
        manager.resetDemoPack()
        assertTrue(manager.isPackInstalled("sat"))

        val removed = manager.removePackMetadata("sat")
        assertTrue(removed)
        assertFalse(manager.isPackInstalled("sat"))
    }
}
