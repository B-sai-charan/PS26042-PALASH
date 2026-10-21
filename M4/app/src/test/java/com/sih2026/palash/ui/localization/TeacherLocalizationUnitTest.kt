package com.sih2026.palash.ui.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TeacherLocalizationUnitTest {

    private val defaultStringsFile = File("src/main/res/values/strings.xml")
    private val hindiStringsFile = File("src/main/res/values-hi/strings.xml")

    private fun loadStringsFromXml(file: File): Map<String, String> {
        assertTrue("String resource file does not exist: ${file.absolutePath}", file.exists())
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = builder.parse(file)
        val nodes = doc.getElementsByTagName("string")
        val map = mutableMapOf<String, String>()
        for (i in 0 until nodes.length) {
            val element = nodes.item(i) as org.w3c.dom.Element
            val name = element.getAttribute("name")
            val text = element.textContent
            map[name] = text
        }
        return map
    }

    @Test
    fun teacherRole_hindiResourcesExistAndContainDevanagari() {
        val defaultStrings = loadStringsFromXml(defaultStringsFile)
        val hindiStrings = loadStringsFromXml(hindiStringsFile)

        val teacherStringKeys = listOf(
            "teacher_pin_title",
            "teacher_pin_subtitle",
            "home_subtitle",
            "home_start_lesson",
            "grade_title",
            "subject_title",
            "subject_math",
            "topic_counting",
            "voice_speak_hindi",
            "voice_translating",
            "voice_santali_header",
            "result_title",
            "result_source_hindi",
            "result_target_santali",
            "pack_title",
            "pack_back_to_home"
        )

        for (key in teacherStringKeys) {
            assertTrue("Default strings missing key: $key", defaultStrings.containsKey(key))
            assertTrue("Hindi strings missing key: $key", hindiStrings.containsKey(key))
            val hindiVal = hindiStrings[key] ?: ""
            assertTrue("Hindi string for key '$key' should not be empty", hindiVal.isNotEmpty())

            // Check that Devanagari characters are present (Unicode range U+0900 to U+097F)
            val hasDevanagari = hindiVal.any { char -> char.code in 0x0900..0x097F }
            assertTrue("Hindi value for '$key' ($hindiVal) should contain Devanagari script", hasDevanagari)
        }
    }

    @Test
    fun teacherRole_keyTranslations_matchExpectedDevanagari() {
        val hindiStrings = loadStringsFromXml(hindiStringsFile)

        assertEquals("शिक्षक पिन सत्यापन", hindiStrings["teacher_pin_title"])
        assertEquals("पाठ शुरू करें", hindiStrings["home_start_lesson"])
        assertEquals("कक्षा चुनें", hindiStrings["grade_title"])
        assertEquals("गणित", hindiStrings["subject_math"])
        assertEquals("गिनती", hindiStrings["topic_counting"])
        assertEquals("हिंदी में बोलें", hindiStrings["voice_speak_hindi"])
        assertEquals("कक्षा सत्र में वापस जाएं", hindiStrings["result_back_to_classroom"])
    }

    @Test
    fun technicalIdentifiers_remainUnchangedInResources() {
        val hindiStrings = loadStringsFromXml(hindiStringsFile)

        assertEquals("पलाश", hindiStrings["app_name"])
        assertTrue(hindiStrings["teacher_pin_placeholder"]?.contains("6") == true)
        assertTrue(hindiStrings["voice_loading_vosk"]?.contains("Vosk") == true)
        assertTrue(hindiStrings["voice_room_cache_notice"]?.contains("Room") == true)
        // FLN is Devanagari transliterated as एफएलएन in Hindi string, or original English in default strings
        assertTrue(hindiStrings["result_open_worksheet"]?.contains("कार्यपत्रिका") == true)
    }

    @Test
    fun teacherScreens_auditNoHardcodedEnglishInCompose() {
        val screensDir = File("src/main/java/com/sih2026/palash/ui/screens")
        assertTrue("Screens directory does not exist", screensDir.exists())

        // Audit Teacher screens (exclude student screens)
        val teacherScreenDirs = listOf("role", "home", "grade", "subject", "voice", "result", "worksheet", "flashcard", "pack")

        val forbiddenEnglishSnippets = listOf(
            "\"Teacher PIN Verification\"",
            "\"AI-Powered Vernacular Learning\"",
            "\"Start Lesson\"",
            "\"Select Grade\"",
            "\"Select Subject & Topic\"",
            "\"Speak in Hindi\"",
            "\"Translating text...\"",
            "\"Translation Result\"",
            "\"Language & Content Packs\""
        )

        screensDir.walkTopDown().filter { file ->
            file.isFile && file.extension == "kt" && 
                teacherScreenDirs.any { file.parentFile?.name == it } && 
                !file.name.contains("Student") &&
                !file.name.contains("RoleSelection")
        }.forEach { ktFile ->
            val content = ktFile.readText()
            for (snippet in forbiddenEnglishSnippets) {
                if (content.contains(snippet)) {
                    println("AUDIT FAILURE in file: ${ktFile.absolutePath} for snippet: $snippet")
                }
                assertTrue(
                    "Teacher file ${ktFile.name} still contains hardcoded English string $snippet",
                    !content.contains(snippet)
                )
            }
        }


    }

}

