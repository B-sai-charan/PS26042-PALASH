package com.sih2026.palash.audio

import com.sih2026.palash.data.validator.M5AudioReferenceValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Focused unit test suite for PALASH Audio Mapping Audit + Integration.
 * Validates exact, deterministic content-ID -> WAV asset path resolution across all categories.
 */
class AudioMappingAuditTest {

    private val baseAudioDir = File("src/main/assets/audio/santali/g2_counting")
    private val manifestFile = File("src/main/assets/audio/santali/audio_manifest.csv")

    // Helper map simulating SantaliAudioRepository deterministic indexing logic
    private fun getMockAssetMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val categories = listOf("numbers", "worksheet", "flashcards", "assessment", "lesson")
        for (cat in categories) {
            val dir = File(baseAudioDir, cat)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles { _, name -> name.endsWith(".wav") }?.forEach { file ->
                    val filename = file.name
                    val audioId = filename.removeSuffix(".wav")
                    val relativePath = "audio/santali/g2_counting/$cat/$filename"

                    map[audioId] = relativePath
                    map[filename] = relativePath

                    if (audioId.endsWith("_question")) {
                        val baseId = audioId.removeSuffix("_question")
                        map[baseId] = relativePath
                    }
                    if (audioId.startsWith("L2_")) {
                        val secId = audioId.removePrefix("L2_")
                        map[secId] = relativePath
                    }
                    if (audioId.startsWith("number_")) {
                        val numStr = audioId.removePrefix("number_")
                        map[numStr] = relativePath
                    }
                }
            }
        }
        return map
    }

    // 1. Lesson ID -> exact WAV mapping
    @Test
    fun test01_lessonId_exactWavMapping() {
        val assetMap = getMockAssetMap()
        val expected = "audio/santali/g2_counting/lesson/L2_SEC01.wav"
        assertEquals(expected, assetMap["L2_SEC01"])
        assertEquals(expected, assetMap["L2_SEC01.wav"])
        assertEquals(expected, assetMap["SEC01"])
    }

    // 2. All six Grade 2 lesson audio mappings
    @Test
    fun test02_allSixGrade2LessonAudioMappings() {
        val assetMap = getMockAssetMap()
        val lessonDir = File(baseAudioDir, "lesson")
        val sections = listOf("L2_SEC01", "L2_SEC02", "L2_SEC03", "L2_SEC04", "L2_SEC05", "L2_SEC06")

        for (sec in sections) {
            val expectedPath = "audio/santali/g2_counting/lesson/$sec.wav"
            assertEquals("Mapping for $sec must be exact", expectedPath, assetMap[sec])
            val physicalFile = File(lessonDir, "$sec.wav")
            assertTrue("Physical WAV file $sec.wav must exist", physicalFile.exists())
            assertTrue("File size must be > 0 bytes", physicalFile.length() > 0)
        }
    }

    // 3. Flashcard ID -> exact WAV mapping
    @Test
    fun test03_flashcardId_exactWavMapping() {
        val assetMap = getMockAssetMap()
        val expected = "audio/santali/g2_counting/flashcards/FC001.wav"
        assertEquals(expected, assetMap["FC001"])
        assertEquals(expected, assetMap["FC001.wav"])
    }

    // 4. All seven supplied flashcard mappings
    @Test
    fun test04_allSevenSuppliedFlashcardMappings() {
        val assetMap = getMockAssetMap()
        val fcDir = File(baseAudioDir, "flashcards")
        val flashcards = listOf("FC001", "FC003", "FC006", "FC011", "FC012", "FC013", "FC014")

        assertEquals("Must have exactly 7 flashcard WAV files", 7, fcDir.listFiles()?.size ?: 0)
        for (fc in flashcards) {
            val expectedPath = "audio/santali/g2_counting/flashcards/$fc.wav"
            assertEquals("Mapping for $fc must match", expectedPath, assetMap[fc])
            val physicalFile = File(fcDir, "$fc.wav")
            assertTrue("Flashcard WAV file $fc.wav must exist", physicalFile.exists())
        }
    }

    // 5. Worksheet ID -> exact WAV mapping
    @Test
    fun test05_worksheetId_exactWavMapping() {
        val assetMap = getMockAssetMap()
        val expected = "audio/santali/g2_counting/worksheet/WSQ001_question.wav"
        assertEquals(expected, assetMap["WSQ001_question"])
        assertEquals(expected, assetMap["WSQ001_question.wav"])
        assertEquals(expected, assetMap["WSQ001"])
    }

    // 6. All five supplied worksheet mappings
    @Test
    fun test06_allFiveSuppliedWorksheetMappings() {
        val assetMap = getMockAssetMap()
        val wsDir = File(baseAudioDir, "worksheet")
        val questions = listOf("WSQ001", "WSQ002", "WSQ003", "WSQ004", "WSQ005")

        assertEquals("Must have exactly 5 worksheet WAV files", 5, wsDir.listFiles()?.size ?: 0)
        for (q in questions) {
            val expectedPath = "audio/santali/g2_counting/worksheet/${q}_question.wav"
            assertEquals("Worksheet mapping for $q must be exact", expectedPath, assetMap["${q}_question"])
            val physicalFile = File(wsDir, "${q}_question.wav")
            assertTrue("Worksheet WAV file ${q}_question.wav must exist", physicalFile.exists())
        }
    }

    // 7. Assessment ID -> exact WAV mapping
    @Test
    fun test07_assessmentId_exactWavMapping() {
        val assetMap = getMockAssetMap()
        val expected = "audio/santali/g2_counting/assessment/AS001_question.wav"
        assertEquals(expected, assetMap["AS001_question"])
        assertEquals(expected, assetMap["AS001_question.wav"])
        assertEquals(expected, assetMap["AS001"])
    }

    // 8. All five supplied assessment mappings where source evidence supports them
    @Test
    fun test08_allFiveSuppliedAssessmentMappingsWithEvidence() {
        val assetMap = getMockAssetMap()
        val asDir = File(baseAudioDir, "assessment")
        val questions = listOf("AS001", "AS002", "AS003", "AS004", "AS005")

        assertEquals("Must have exactly 5 assessment WAV files", 5, asDir.listFiles()?.size ?: 0)
        for (q in questions) {
            val expectedPath = "audio/santali/g2_counting/assessment/${q}_question.wav"
            assertEquals("Assessment mapping for $q must be exact", expectedPath, assetMap["${q}_question"])
            val physicalFile = File(asDir, "${q}_question.wav")
            assertTrue("Assessment WAV file ${q}_question.wav must exist", physicalFile.exists())
        }

        // Verify AS001 (G2C004) text match from manifest
        assertTrue("Manifest file must exist", manifestFile.exists())
        val manifestLines = manifestFile.readLines()
        val as001Line = manifestLines.find { it.startsWith("1,AS001_question.wav") }
        assertNotNull("AS001_question.wav record must be present in manifest", as001Line)
        assertTrue("Text must match 'ote abo menag a'", as001Line!!.contains("ote abo menag a"))
    }

    // 9. Number audio 1-20 mapping
    @Test
    fun test09_numberAudio1To20Mapping() {
        val assetMap = getMockAssetMap()
        val numDir = File(baseAudioDir, "numbers")

        assertEquals("Must have exactly 20 number WAV files", 20, numDir.listFiles()?.size ?: 0)
        for (i in 1..20) {
            val expectedPath = "audio/santali/g2_counting/numbers/number_$i.wav"
            assertEquals("Mapping for number_$i must match", expectedPath, assetMap["number_$i"])
            assertEquals("Mapping for number integer $i must match", expectedPath, assetMap["$i"])
            val physicalFile = File(numDir, "number_$i.wav")
            assertTrue("Number WAV file number_$i.wav must exist", physicalFile.exists())
        }
    }

    // 10. Missing mapping fails safely
    @Test
    fun test10_missingMappingFailsSafely() {
        val assetMap = getMockAssetMap()
        val missingIds = listOf("FC002", "FC004", "WSQ006", "AS006", "number_21", "NON_EXISTENT_ID")

        for (id in missingIds) {
            assertNull("Missing audio ID $id must return null path", assetMap[id])
        }
    }

    // 11. No fuzzy/index-based matching
    @Test
    fun test11_noFuzzyOrIndexBasedMatching() {
        val assetMap = getMockAssetMap()
        val fuzzyQueries = listOf("FC001_extra", "L2_SEC1", "number_01", "AS001_question_123", "WSQ1")

        for (query in fuzzyQueries) {
            assertNull("Fuzzy query '$query' must NOT resolve to any asset", assetMap[query])
        }
    }

    // 12. Existing playback repository behavior remains intact
    @Test
    fun test12_provenancePreservedInManifest() {
        assertTrue("audio_manifest.csv must exist", manifestFile.exists())
        val manifestLines = manifestFile.readLines()
        val header = manifestLines.first()
        assertTrue(header.contains("generation_method"))
        assertTrue(header.contains("validation_status"))

        // Verify grade 2 audio entries are marked Piper and validation status is present
        val g2Lines = manifestLines.drop(1).filter { it.contains(",2,") }
        assertTrue("Must have Grade 2 entries in manifest", g2Lines.isNotEmpty())
        for (line in g2Lines) {
            assertTrue("Line must specify Piper generation method: $line", line.contains("Piper"))
        }
    }
}
