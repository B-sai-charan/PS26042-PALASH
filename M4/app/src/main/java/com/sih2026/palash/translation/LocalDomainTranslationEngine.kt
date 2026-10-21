package com.sih2026.palash.translation

import android.content.Context
import com.sih2026.palash.data.local.dao.TranslationCacheDao
import com.sih2026.palash.data.local.db.DatabaseProvider

/**
 * Local domain translation engine querying Room local database / offline content packs.
 * Returns verified translations if available in local database; otherwise returns TranslationStatus.UNAVAILABLE.
 * Strictly avoids inventing or fabricating unverified translations.
 */
class LocalDomainTranslationEngine(
    private val translationDao: TranslationCacheDao?
) : TranslationEngine {

    companion object {
        private val staticCurriculumMap = mapOf(
            "एक" to "ᱢᱤᱫ",
            "दो" to "ᱵᱟᱨ",
            "तीन" to "ᱯᱮ",
            "चार" to "ᱯᱚᱱ",
            "पाँच" to "ᱢᱚᱬᱮ",
            "छह" to "ᱛᱩᱨᱩᱭ",
            "सात" to "ᱮᱭᱟᱭ",
            "आठ" to "ᱤᱨᱟᱹᱞ",
            "नौ" to "ᱟᱨᱮ",
            "दस" to "ᱜᱮᱞ",
            "पाँच आम गिनो" to "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।",
            "गिनती सीखें" to "ᱞᱮᱠᱷᱟ ᱪᱮᱫᱚᱜ ᱢᱮ।",
            "1 से 10 तक संख्याएँ" to "᱑ ᱠᱷᱚᱱ ᱑᱐ ᱦᱟᱹᱵᱤᱡ ᱮᱞᱠᱚ",
            "आइए 1 से 10 तक गिनती सीखें" to "1 ᱠᱷᱚᱱ 10 ᱫᱷᱟᱵᱤᱡ ᱮᱞ ᱪᱮᱫ ᱢᱮ।",
            "हर वस्तु को एक-एक करके गिनो और सही संख्या बोलो" to "ᱯᱨᱚᱛᱤ ᱡᱤᱱᱤᱥ ᱢᱤᱫ ᱢᱤᱫ ᱛᱮ ᱮᱞ ᱟᱨ ᱥᱟᱹᱨᱤ ᱮᱞ ᱢᱮᱱ ᱢᱮ।",
            "संख्या, उसका नाम और वस्तुओं की गिनती देखो" to "ᱮᱞ, ᱤᱧ ᱧᱩᱛᱩᱢ ᱟᱨ ᱡᱤᱱᱤᱥ ᱮᱞ ᱧᱮᱞ ᱢᱮ।",
            "अपने आसपास 5 वस्तुएँ खोजो और उन्हें गिनो" to "ᱟᱢ ᱟᱬᱮᱵ 5 ᱡᱤᱱᱤᱥ ᱯᱟᱧᱡᱟ ᱟᱨ ᱩᱱᱠᱩ ᱮᱞ ᱢᱮ।",
            "सही संख्या पहचानो और गिनती का अभ्यास करो" to "ᱥᱟᱹᱨᱤ ᱮᱞ ᱩᱯᱨᱩᱢ ᱢᱮ ᱟᱨ ᱮᱞ ᱪᱮᱫ ᱢᱮ।",
            "गिनती करके सही संख्या चुनो" to "ᱥᱟᱹᱨᱤ ᱮᱞ ᱩᱯᱨᱩᱢ ᱢᱮ ᱟᱨ ᱮᱞ ᱪᱮᱫ ᱢᱮ।",
            "कितने आम हैं" to "ᱚᱛᱮ ᱟᱸᱵᱚ ᱢᱮᱱᱟᱜᱼᱟ?",
            "चार के बाद कौन-सी संख्या आती है" to "ᱯᱩᱱ ᱛᱟᱭᱚᱢ ᱚᱠᱟ ᱟᱸᱠ ᱦᱩᱭ ᱟᱭ?",
            "सबसे बड़ी संख्या पर घेरा लगाओ" to "ᱢᱟᱨᱟᱝ ᱟᱸᱠ ᱨᱮ ᱴᱟᱨᱟ ᱮᱦᱚᱵ ᱢᱮ।",
            "सबसे छोटी संख्या कौन-सी है" to "ᱦᱩᱰᱤᱧ ᱟᱸᱠ ᱚᱠᱟ ᱠᱟᱱᱟ?",
            "संख्या 7 को अंकों में लिखो" to "ᱮᱭᱟᱭ ᱟᱸᱠ ᱚᱞ ᱢᱮ।",
            "एक से दस तक गिनो" to "ᱢᱤᱫ ᱠᱷᱚᱱ ᱜᱮᱞ ᱚᱫᱚᱠ ᱨᱮᱠᱚᱲ ᱢᱮ।",
            "एक + दो = तीन" to "ᱢᱤᱫ + ᱵᱟᱨ = ᱯᱮ",
            "चार - तीन = एक" to "ᱯᱩᱱ - ᱯᱮ = ᱢᱤᱫ",
            "दो x चार = आठ" to "ᱵᱟᱨ x ᱯᱩᱱ = ᱤᱨᱟᱹᱞ",
            "छह ÷ दो = तीन" to "ᱛᱩᱨᱩᱭ ÷ ᱵᱟᱨ = ᱯᱮ"
        )

        fun normalizeForMatching(text: String): String {
            if (text.isBlank()) return ""
            return text.trim()
                .replace(Regex("[\u200B\u200C\u200D\uFEFF]"), "") // Strip zero-width Unicode chars
                .replace(Regex("\\s+"), " ")                     // Collapse multi-spaces
                .replace(Regex("[।\\.\\?!,]+$"), "")             // Strip trailing sentence punctuation
                .trim()
        }
    }

    constructor(context: Context) : this(
        try {
            DatabaseProvider.getDatabase(context).translationCacheDao()
        } catch (e: Exception) {
            null
        }
    )

    override suspend fun translate(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String
    ): TranslationResult {
        val startTime = System.currentTimeMillis()
        val normalizedInput = normalizeForMatching(sourceText)

        if (normalizedInput.isEmpty() ||
            normalizedInput.equals("No speech recognized", ignoreCase = true) ||
            normalizedInput == "कोई आवाज़ नहीं पहचानी गई"
        ) {
            return TranslationResult(
                sourceText = sourceText,
                translatedText = null,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                status = TranslationStatus.UNAVAILABLE,
                sourceType = TranslationSourceType.UNKNOWN,
                processingTimeMs = System.currentTimeMillis() - startTime,
                errorMessage = "इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है"
            )
        }

        try {
            val dao = translationDao

            // Priority 1: Verified Curriculum Dictionary Ground-Truth Match
            val verifiedMatch = CurriculumDictionary.getVerifiedTranslation(sourceText.trim())
                ?: CurriculumDictionary.getVerifiedTranslation(normalizedInput)
            if (verifiedMatch != null) {
                val elapsedTime = System.currentTimeMillis() - startTime
                return TranslationResult(
                    sourceText = sourceText,
                    translatedText = verifiedMatch,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    status = TranslationStatus.VERIFIED_LOCAL,
                    sourceType = TranslationSourceType.LOCAL_CONTENT_PACK,
                    processingTimeMs = elapsedTime
                )
            }

            // Priority 2: Exact or normalized match from Room Database
            var record = dao?.findTranslation(sourceText.trim(), sourceLanguage, targetLanguage)
            if (record == null && dao != null) {
                record = dao.findTranslation(normalizedInput, sourceLanguage, targetLanguage)
            }

            val isValidRecord = record != null &&
                    !record.targetText.isNullOrBlank() &&
                    !record.targetText.contains("pending", ignoreCase = true) &&
                    !record.targetText.contains("notice", ignoreCase = true) &&
                    record.validationStatus != ValidationStatus.REJECTED.name

            if (isValidRecord && record != null) {
                val elapsedTime = System.currentTimeMillis() - startTime
                return TranslationResult(
                    sourceText = sourceText,
                    translatedText = record.targetText,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    status = TranslationStatus.VERIFIED_LOCAL,
                    sourceType = TranslationSourceType.LOCAL_CONTENT_PACK,
                    processingTimeMs = elapsedTime
                )
            }

            // Priority 3: Legacy Static Curriculum Dictionary Match
            val staticMatch = staticCurriculumMap[sourceText.trim()] ?: staticCurriculumMap[normalizedInput]
            if (staticMatch != null) {
                val elapsedTime = System.currentTimeMillis() - startTime
                return TranslationResult(
                    sourceText = sourceText,
                    translatedText = staticMatch,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    status = TranslationStatus.VERIFIED_LOCAL,
                    sourceType = TranslationSourceType.LOCAL_CONTENT_PACK,
                    processingTimeMs = elapsedTime
                )
            }

            // Priority 4: Unavailable (Honest Hindi Message)
            val elapsedTime = System.currentTimeMillis() - startTime
            return TranslationResult(
                sourceText = sourceText,
                translatedText = null,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                status = TranslationStatus.UNAVAILABLE,
                sourceType = TranslationSourceType.UNKNOWN,
                processingTimeMs = elapsedTime,
                errorMessage = "इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है"
            )
        } catch (e: Exception) {
            val elapsedTime = System.currentTimeMillis() - startTime
            return TranslationResult(
                sourceText = sourceText,
                translatedText = null,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                status = TranslationStatus.ERROR,
                sourceType = TranslationSourceType.UNKNOWN,
                processingTimeMs = elapsedTime,
                errorMessage = "इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है"
            )
        }
    }
}
