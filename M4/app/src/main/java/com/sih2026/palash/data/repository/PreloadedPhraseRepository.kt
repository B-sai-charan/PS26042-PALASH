package com.sih2026.palash.data.repository

import java.util.Locale

/**
 * Data model for preloaded classroom phrases sourced from phrases.txt.xlsx.
 */
data class PreloadedPhraseEntity(
    val id: String,
    val hindiText: String,
    val normalizedHindi: String,
    val santaliText: String,
    val romanizedSantali: String? = null,
    val audioFilename: String? = null,
    val source: String = "phrases.txt.xlsx",
    val validationStatus: String = "EXCEL_PRELOADED_AUTHORITATIVE",
    val notes: String? = null
)

/**
 * Authoritative in-memory / preloaded repository for phrases from phrases.txt.xlsx.
 * Operates offline with zero network or server dependency.
 */
object PreloadedPhraseRepository {

    /**
     * Normalizes Hindi text for exact deterministic matching:
     * - Trims surrounding whitespace
     * - Collapses internal consecutive whitespace to single space
     * - Strips zero-width characters (ZWSP, ZWNJ, ZWJ, BOM)
     * - Strips trailing Devanagari danda (।), double danda (॥), periods, exclamation, and question marks
     */
    fun normalizeHindi(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .trim()
            .replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "")
            .replace(Regex("\\s+"), " ")
            .trimEnd { it == '।' || it == '॥' || it == '.' || it == '?' || it == '!' || it.isWhitespace() }
            .trim()
    }

    // In-memory static dictionary initialized with all 15 authoritative rows from phrases.txt.xlsx
    private val PRELOADED_PHRASES: List<PreloadedPhraseEntity> = listOf(
        PreloadedPhraseEntity(
            id = "P001",
            hindiText = "सभी बच्चे बैठ जाओ।",
            normalizedHindi = "सभी बच्चे बैठ जाओ",
            santaliText = "ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ, ᱫᱩᱲᱩᱵ ᱢᱮ᱾",
            notes = "Row 1 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P002",
            hindiText = "आज हम क्या सीखेंगे?",
            normalizedHindi = "आज हम क्या सीखेंगे",
            santaliText = "ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱪᱮᱫ ᱵᱚᱱ ᱪᱮᱫᱚᱜᱼᱟ?",
            notes = "Row 2 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P003",
            hindiText = "नमस्ते बच्चों।",
            normalizedHindi = "नमस्ते बच्चों",
            santaliText = "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ ᱾",
            notes = "Row 3 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P004",
            hindiText = "आप सभी चुप रहें।",
            normalizedHindi = "आप सभी चुप रहें",
            santaliText = "ᱟᱯᱮ ᱡᱚᱛᱚ ᱦᱚᱲ ᱫᱟᱭᱟ ᱠᱟᱛᱮ ᱛᱷᱤᱨ ᱛᱟᱦᱮᱱ ᱢᱮ᱾",
            notes = "Row 4 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P005",
            hindiText = "अपनी पुस्तकें खोलो",
            normalizedHindi = "अपनी पुस्तकें खोलो",
            santaliText = "ᱟᱢᱟᱜ ᱯᱚᱛᱚᱵ ᱡᱷᱤᱡᱽ ᱢᱮ",
            notes = "Row 5 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P006",
            hindiText = "शुभकामनाएं",
            normalizedHindi = "शुभकामनाएं",
            santaliText = "ᱥᱟᱹᱜᱩᱱ ᱥᱟᱱᱟᱝ",
            notes = "Row 6 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P007",
            hindiText = "तुम्हें कितने नंबर मिले?",
            normalizedHindi = "तुम्हें कितने नंबर मिले",
            santaliText = "ᱟᱢ ᱛᱤᱱᱟᱹᱜ ᱪᱤᱱᱦᱟᱹ ᱮᱢ ᱧᱟᱢ ᱠᱮᱫᱟ?",
            notes = "Row 7 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P008",
            hindiText = "कृपया इसे एक बार फिर समझा दें।",
            normalizedHindi = "कृपया इसे एक बार फिर समझा दें",
            santaliText = "ᱫᱟᱭᱟ ᱠᱟᱛᱮ ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫ ᱫᱷᱟᱣ ᱫᱚᱦᱲᱟ ᱞᱟᱹᱭ ᱢᱮ᱾",
            notes = "Row 8 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P009",
            hindiText = "क्या मैं शौचालय जा सकता हुँ?",
            normalizedHindi = "क्या मैं शौचालय जा सकता हुँ",
            santaliText = "ᱪᱮᱫ ᱤᱧ ᱴᱚᱭᱞᱮᱴ ᱥᱮᱱ ᱫᱟᱲᱮᱭᱟᱜᱼᱟᱹᱧ?",
            notes = "Row 9 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P010",
            hindiText = "चुपचाप बैठें",
            normalizedHindi = "चुपचाप बैठें",
            santaliText = "ᱛᱷᱤᱨ ᱛᱷᱤᱨ ᱫᱩᱲᱩᱵ ᱢᱮ",
            notes = "Row 10 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P011",
            hindiText = "आप सभी अच्छी तरह तैयारी करें।",
            normalizedHindi = "आप सभी अच्छी तरह तैयारी करें",
            santaliText = "ᱟᱯᱮ ᱡᱚᱛᱚ ᱦᱚᱲ ᱱᱟᱯᱟᱭ ᱛᱮ ᱥᱟᱯᱲᱟᱣᱚᱜ ᱢᱮ᱾",
            notes = "Row 11 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P012",
            hindiText = "शोर मत करो.",
            normalizedHindi = "शोर मत करो",
            santaliText = "ᱠᱟᱞᱠᱟᱞᱟᱣ ᱟᱞᱚᱢ ᱛᱮᱭᱟᱨᱟ᱾",
            notes = "Row 12 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P013",
            hindiText = "नमस्ते बच्चों, आज हम गणित सीखेंगे।",
            normalizedHindi = "नमस्ते बच्चों, आज हम गणित सीखेंगे",
            santaliText = "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱮᱞᱠᱷᱟ ᱵᱚᱱ ᱪᱮᱫᱟ᱾",
            notes = "Row 13 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P014",
            hindiText = "इसे ज़रूर लिखें।",
            normalizedHindi = "इसे ज़रूर लिखें",
            santaliText = "ᱫᱟᱭᱟ ᱠᱟᱛᱮ ᱱᱚᱣᱟ ᱚᱞ ᱢᱮ᱾",
            notes = "Row 14 from Sheet1"
        ),
        PreloadedPhraseEntity(
            id = "P015",
            hindiText = "आप सभी की कल परीक्षा है।",
            normalizedHindi = "आप सभी की कल परीक्षा है",
            santaliText = "ᱟᱯᱮ ᱥᱟᱱᱟᱢ ᱠᱚᱣᱟᱜ ᱜᱟᱯᱟ ᱵᱤᱱᱤᱰ ᱢᱮᱱᱟᱜᱼᱟ ᱾",
            notes = "Row 15 from Sheet1"
        )
    )

    private val PHRASE_MAP: Map<String, PreloadedPhraseEntity> =
        PRELOADED_PHRASES.associateBy { it.normalizedHindi }

    /**
     * Looks up an exact matching phrase from phrases.txt.xlsx.
     * Returns the PreloadedPhraseEntity if matched, null otherwise.
     */
    fun findExactHindiPhrase(rawHindi: String?): PreloadedPhraseEntity? {
        if (rawHindi.isNullOrBlank()) return null
        val normalized = normalizeHindi(rawHindi)
        return PHRASE_MAP[normalized]
    }

    /**
     * Returns true if the given Hindi text exists in the preloaded Excel phrases dataset.
     */
    fun isPreloaded(rawHindi: String?): Boolean = findExactHindiPhrase(rawHindi) != null

    /**
     * Returns all preloaded phrase entries.
     */
    fun getAllPhrases(): List<PreloadedPhraseEntity> = PRELOADED_PHRASES

    /**
     * Returns total count of preloaded entries.
     */
    fun getPhraseCount(): Int = PRELOADED_PHRASES.size

    /**
     * Randomly selects one record from P001-P015 for unknown Hindi input fallback.
     * Guaranteed to return a valid candidate among all 15 preloaded records.
     */
    fun getRandomPhrase(random: kotlin.random.Random = kotlin.random.Random.Default): PreloadedPhraseEntity {
        val index = random.nextInt(PRELOADED_PHRASES.size)
        return PRELOADED_PHRASES[index]
    }

    /**
     * Looks up exact matching phrase or falls back to a controlled random P001-P015 record.
     */
    fun findOrFallbackPhrase(
        rawHindi: String?,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): PreloadedLookupResult {
        val exact = findExactHindiPhrase(rawHindi)
        return if (exact != null) {
            PreloadedLookupResult(
                entity = exact,
                isExactMatch = true
            )
        } else {
            PreloadedLookupResult(
                entity = getRandomPhrase(random),
                isExactMatch = false
            )
        }
    }
}

/**
 * Result wrapper distinguishing exact match vs controlled demo fallback.
 */
data class PreloadedLookupResult(
    val entity: PreloadedPhraseEntity,
    val isExactMatch: Boolean
)
