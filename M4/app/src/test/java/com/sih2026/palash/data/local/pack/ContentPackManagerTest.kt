package com.sih2026.palash.data.local.pack

import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LanguagePackStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPackManagerTest {

    @Test
    fun testDemoContentPackFactoryCreation() {
        val pack = DemoContentPackFactory.createSantaliDemoPack()

        assertNotNull(pack.metadata)
        assertEquals("sat", pack.metadata.languageCode)
        assertEquals("Santali", pack.metadata.languageName)
        assertEquals(DemoContentPackFactory.SANTALI_VERSION, pack.metadata.version)
        assertEquals(LanguagePackStatus.INSTALLED.name, pack.metadata.status)
        assertEquals("demo-checksum-sat-v0.1", pack.metadata.checksum)

        assertTrue(pack.lessons.isNotEmpty())
        assertTrue(pack.worksheets.isNotEmpty())
        assertTrue(pack.translations.isNotEmpty())

        assertTrue(pack.translations.size >= 28)
        val sampleTranslation = pack.translations.first()
        assertEquals("model_generated_unvalidated", sampleTranslation.validationStatus)
        org.junit.Assert.assertNotNull(sampleTranslation.targetText)
    }

    @Test
    fun testLanguagePackStatusEnumValues() {
        val statuses = LanguagePackStatus.values().map { it.name }
        assertTrue(statuses.contains("NOT_INSTALLED"))
        assertTrue(statuses.contains("INSTALLED"))
        assertTrue(statuses.contains("UPDATE_AVAILABLE"))
        assertTrue(statuses.contains("INVALID"))
    }

    @Test
    fun testContentPackStructure() {
        val metadata = LanguagePackMetadataEntity(
            languageCode = "hi",
            languageName = "Hindi",
            script = "Devanagari",
            version = "1.0",
            status = LanguagePackStatus.INSTALLED.name,
            checksum = "test-check"
        )
        val pack = ContentPack(
            metadata = metadata,
            lessons = emptyList(),
            worksheets = emptyList(),
            translations = emptyList()
        )

        assertEquals("hi", pack.metadata.languageCode)
        assertEquals("Devanagari", pack.metadata.script)
    }
}
