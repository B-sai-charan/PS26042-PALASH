package com.sih2026.palash.data.local.pack

import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LanguagePackStatus
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.local.entity.WorksheetEntity

object DemoContentPackFactory {
    const val SANTALI_CODE = "sat"
    const val SANTALI_VERSION = "0.1-demo"
    const val DEMO_CHECKSUM = "demo-checksum-sat-v0.1"

    fun createSantaliDemoPack(): ContentPack {
        val metadata = LanguagePackMetadataEntity(
            languageCode = SANTALI_CODE,
            languageName = "Santali",
            script = "Ol Chiki / Devanagari",
            version = SANTALI_VERSION,
            status = LanguagePackStatus.INSTALLED.name,
            checksum = DEMO_CHECKSUM,
            installedAt = System.currentTimeMillis()
        )

        val lessons = listOf(
            LessonEntity(
                id = "lesson_g2_math_counting",
                grade = "Grade 2",
                subject = "Mathematics",
                topic = "Counting",
                title = "Counting Apples & Objects (1-10)",
                languageCode = "hi",
                contentJson = """{"hindi_phrase":"पाँच आम गिनो。","notice":"${DemoData.sampleSantaliOutputNotice}"}"""
            ),
            LessonEntity(
                id = "lesson_g2_lang_letters",
                grade = "Grade 2",
                subject = "Language",
                topic = "Letters",
                title = "Foundational Vowels & Alphabets",
                languageCode = "hi",
                contentJson = """{"hindi_phrase":"अक्षर पहचानो।","notice":"${DemoData.sampleSantaliOutputNotice}"}"""
            )
        )

        val worksheets = DemoData.sampleWorksheetQuestions.map { q ->
            WorksheetEntity(
                id = q.id,
                grade = q.grade,
                subject = q.subject,
                topic = q.topic,
                title = "${q.grade} ${q.subject} - ${q.topic}",
                sourceLanguage = "hi",
                targetLanguage = "sat",
                contentJson = """{"prompt":"${q.visualPrompt}","question":"${q.questionText}","hindi":"${q.hindiInstruction}","santali":"${q.santaliInstructionNotice}","correct":"${q.correctAnswer}"}""",
                version = SANTALI_VERSION
            )
        }

        val curriculumEntries = listOf(
            Pair("एक", "ᱢᱤᱫ"),
            Pair("दो", "ᱵᱟᱨ"),
            Pair("तीन", "ᱯᱮ"),
            Pair("चार", "ᱯᱚᱱ"),
            Pair("पाँच", "ᱢᱚᱬᱮ"),
            Pair("छह", "ᱛᱩᱨᱩᱭ"),
            Pair("सात", "ᱮᱭᱟᱭ"),
            Pair("आठ", "ᱤᱨᱟᱹᱞ"),
            Pair("नौ", "ᱟᱨᱮ"),
            Pair("दस", "ᱜᱮᱞ"),
            Pair("पाँच आम गिनो।", "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।"),
            Pair("पाँच आम गिनो", "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।"),
            Pair("गिनती सीखें", "ᱞᱮᱠᱷᱟ ᱪᱮᱫᱚᱜ ᱢᱮ।"),
            Pair("1 से 10 तक संख्याएँ", "᱑ ᱠᱷᱚᱱ ᱑᱐ ᱦᱟᱹᱵᱤᱡ ᱮᱞᱠᱚ"),
            Pair("आइए 1 से 10 तक गिनती सीखें।", "1 ᱠᱷᱚᱱ 10 ᱫᱷᱟᱵᱤᱡ ᱮᱞ ᱪᱮᱫ ᱢᱮ।"),
            Pair("हर वस्तु को एक-एक करके गिनो और सही संख्या बोलो।", "ᱯᱨᱚᱛᱤ ᱡᱤᱱᱤᱥ ᱢᱤᱫ ᱢᱤᱫ ᱛᱮ ᱮᱞ ᱟᱨ ᱥᱟᱹᱨᱤ ᱮᱞ ᱢᱮᱱ ᱢᱮ।"),
            Pair("संख्या, उसका नाम और वस्तुओं की गिनती देखो।", "ᱮᱞ, ᱤᱧ ᱧᱩᱛᱩᱢ ᱟᱨ ᱡᱤᱱᱤᱥ ᱮᱞ ᱧᱮᱞ ᱢᱮ।"),
            Pair("अपने आसपास 5 वस्तुएँ खोजो और उन्हें गिनो।", "ᱟᱢ ᱟᱬᱮᱵ 5 ᱡᱤᱱᱤᱥ ᱯᱟᱧᱡᱟ ᱟᱨ ᱩᱱᱠᱩ ᱮᱞ ᱢᱮ।"),
            Pair("सही संख्या पहचानो और गिनती का अभ्यास करो।", "ᱥᱟᱹᱨᱤ ᱮᱞ ᱩᱯᱨᱩᱢ ᱢᱮ ᱟᱨ ᱮᱞ ᱪᱮᱫ ᱢᱮ।"),
            Pair("गिनती करके सही संख्या चुनो।", "ᱥᱟᱹᱨᱤ ᱮᱞ ᱩᱯᱨᱩᱢ ᱢᱮ ᱟᱨ ᱮᱞ ᱪᱮᱫ ᱢᱮ।"),
            Pair("कितने आम हैं?", "ᱚᱛᱮ ᱟᱸᱵᱚ ᱢᱮᱱᱟᱜᱼᱟ?"),
            Pair("चार के बाद कौन-सी संख्या आती है?", "ᱯᱩᱱ ᱛᱟᱭᱚᱢ ᱚᱠᱟ ᱟᱸᱠ ᱦᱩᱭ ᱟᱭ?"),
            Pair("सबसे बड़ी संख्या पर घेरा लगाओ।", "ᱢᱟᱨᱟᱝ ᱟᱸᱠ ᱨᱮ ᱴᱟᱨᱟ ᱮᱦᱚᱵ ᱢᱮ।"),
            Pair("सबसे छोटी संख्या कौन-सी है?", "ᱦᱩᱰᱤᱧ ᱟᱸᱠ ᱚᱠᱟ ᱠᱟᱱᱟ?"),
            Pair("संख्या 7 को अंकों में लिखो।", "ᱮᱭᱟᱭ ᱟᱸᱠ ᱚᱞ ᱢᱮ।"),
            Pair("एक से दस तक गिनो।", "ᱢᱤᱫ ᱠᱷᱚᱱ ᱜᱮᱞ ᱚᱫᱚᱠ ᱨᱮᱠᱚᱲ ᱢᱮ।"),
            Pair("एक + दो = तीन", "ᱢᱤᱫ + ᱵᱟᱨ = ᱯᱮ"),
            Pair("चार - तीन = एक", "ᱯᱩᱱ - ᱯᱮ = ᱢᱤᱫ"),
            Pair("दो x चार = आठ", "ᱵᱟᱨ x ᱯᱩᱱ = ᱤᱨᱟᱹᱞ"),
            Pair("छह ÷ दो = तीन", "ᱛᱩᱨᱩᱭ ÷ ᱵᱟᱨ = ᱯᱮ")
        )

        val translations = curriculumEntries.mapIndexed { index, (hindi, santali) ->
            TranslationCacheEntity(
                id = "trans_g2_curr_${index + 1}",
                sourceLanguage = "hi",
                targetLanguage = SANTALI_CODE,
                sourceText = hindi,
                targetText = santali,
                script = "Ol Chiki",
                validationStatus = "model_generated_unvalidated",
                validatorType = null,
                validatorNameOrId = null,
                validationDate = 0L,
                sourceReference = "PALASH FLN Grade 2 Curriculum Baseline",
                license = "Open Educational / Model Generated Content",
                notes = "Existing Grade 2 FLN curriculum baseline content"
            )
        }

        return ContentPack(
            metadata = metadata,
            lessons = lessons,
            worksheets = worksheets,
            translations = translations
        )
    }
}
