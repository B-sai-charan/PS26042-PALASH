package com.sih2026.palash.translation

import com.sih2026.palash.data.local.entity.TranslationCacheEntity

/**
 * Verified classroom curriculum dictionary for PALASH.
 * High-confidence ground-truth translations validated by team / native speakers.
 */
object CurriculumDictionary {

    private val verifiedPhrases: Map<String, String> = mapOf(
        // 15 Verified Classroom Phrases
        "सभी बच्चे बैठ जाओ।" to "ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ ᱫᱩᱲᱩᱵ ᱯե᱾",
        "आज हम क्या सीखेंगे?" to "ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱪᱮᱫ ᱵᱚᱱ ᱪᱮᱫᱟᱜᱼᱟ?",
        "नमस्ते बच्चों।" to "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ᱾",
        "आप सभी चुप रहें।" to "ᱟᱯᱮ ᱡᱚᱛᱚ ᱦᱚᱲ ᱛᱷᱤᱨ ᱠᱚᱜ ᱯե᱾",
        "अपनी पुस्तकें खोलो" to "ᱟᱯᱱᱟᱨ ᱯᱩᱛᱷᱤ ᱨᱟᱲᱟᱭ ᱯե",
        "शुभकामनाएं" to "ᱥᱟᱹᱜᱩᱱ ᱥᱟᱱᱟᱢ",
        "तुम्हें कितने नंबर मिले?" to "ᱟᱢ ᱛᱤᱱᱟᱹᱜ ᱱᱚᱢᱵᱚᱨ ᱮᱢ ᱧᱟᱢ ᱠᱮᱫᱼᱟ?",
        "कृपया इसे एक बार फिर समझा दें।" to "ᱫᱟᱭᱟ ᱠᱟᱛᱮ ᱱᱚᱶᱟ ᱟᱨ ᱢᱤᱫ ᱫᱷᱟᱣ ᱵᱩᱡᱷᱟᱹᱣ ᱞᱟᱹᱭ ᱯե᱾",
        "क्या मैं शौचालय जा सकता हूँ?" to "ᱪᱮᱫ ᱤᱧ ᱨᱟᱪᱟ ᱛᱮᱧ ᱥᱮᱱ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ?",
        "चुपचाप बैठें" to "ᱛᱷᱤᱨ ᱛᱷᱤᱨ ᱫᱩᱲᱩᱵ ᱯե",
        "आप सभी अच्छी तरह तैयारी करें।" to "ᱟᱯᱮ ᱡᱚᱛᱚ ᱦᱚᱲ ᱱᱟᱯᱟᱭ ᱛᱮ ᱥᱟᱯᱲᱟᱣ ᱠᱚᱜ ᱯե᱾",
        "شور मत करो।" to "ᱦᱟᱲᱵᱟᱲᱟᱣ ᱟᱞᱚᱯᱮ ᱦᱩᱭ ᱚᱪᱚᱭᱟ᱾",
        "नमस्ते बच्चों, आज हम गणित सीखेंगे।" to "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱞᱮᱠᱷᱟ ᱵᱚᱱ ᱪᱮᱫᱟᱜᱼᱟ᱾",
        "इसे ज़रूर लिखें।" to "ᱱᱚᱶᱟ ᱡᱟᱹᱨᱩᱲ ᱚᱞ ᱯե᱾",
        "आप सभी की कल परीक्षा है।" to "ᱟᱯᱮ ᱥᱟᱱᱟᱢ ᱠᱚᱣᱟᱜ ᱜᱟᱯᱟ ᱵᱤᱱᱤᱰ ᱢᱮᱱᱟᱜᱼᱟ᱾",

        // 10 Verified Number Mappings
        "एक" to "ᱢᱤᱫ",
        "दो" to "ᱵᱟᱨ",
        "तीन" to "ᱯᱮ",
        "चार" to "ᱯᱚᱱ",
        "पाँच" to "ᱢᱚᱬᱮ",
        "छह" to "ᱛᱩᱨᱩᱭ",
        "सात" to "ᱮᱭᱟᱭ",
        "आठ" to "ᱤᱨᱟᱹᱞ",
        "नौ" to "ᱟᱨᱮ",
        "दस" to "ᱜᱮᱞ"
    )

    fun normalizeText(text: String): String {
        if (text.isBlank()) return ""
        return text.trim().replace(Regex("\\s+"), " ")
    }

    fun isVerified(text: String): Boolean {
        val normalized = normalizeText(text)
        return verifiedPhrases.containsKey(normalized)
    }

    fun getVerifiedTranslation(text: String): String? {
        val normalized = normalizeText(text)
        return verifiedPhrases[normalized]
    }

    fun getAllVerifiedPhrases(): Map<String, String> {
        return verifiedPhrases
    }

    fun getVerifiedEntity(text: String): TranslationCacheEntity? {
        val target = getVerifiedTranslation(text) ?: return null
        val normalized = normalizeText(text)
        return TranslationCacheEntity(
            id = "curriculum_verified_${normalized.hashCode()}",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck",
            sourceText = normalized,
            targetText = target,
            script = "Ol Chiki",
            validationStatus = ValidationStatus.OFFICIAL_SOURCE.name,
            validatorType = null,
            validatorNameOrId = null,
            validationDate = 0L,
            sourceReference = "PALASH FLN Ground-Truth Curriculum",
            license = null,
            notes = "Verified native/team classroom curriculum mapping",
            createdAt = System.currentTimeMillis()
        )
    }
}
