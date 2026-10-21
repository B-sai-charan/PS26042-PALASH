package com.sih2026.palash.tts

/**
 * Deterministic Ol Chiki to Romanized Santali transliteration engine
 * adhering strictly to the Authoritative Romanization Mapping and Word-Pause Rule.
 */
object SantaliOlChikiTransliteration {

    private const val TAG = "LivePiperTTS"

    private val mapping = mapOf(
        "ᱚ" to "o",
        "ᱛ" to "t",
        "ᱜ" to "g",
        "ᱝ" to "ng",
        "ᱞ" to "l",
        "ᱟ" to "a",
        "ᱠ" to "k",
        "ᱡ" to "j",
        "ᱢ" to "m",
        "ᱣ" to "w",
        "ᱤ" to "i",
        "ᱥ" to "s",
        "ᱦ" to "h",
        "ᱧ" to "ny",
        "ᱨ" to "r",
        "ᱩ" to "u",
        "ᱪ" to "ch",
        "ᱫ" to "d",
        "ᱬ" to "n",
        "ᱭ" to "y",
        "ᱮ" to "e",
        "ᱯ" to "p",
        "ᱰ" to "d",
        "ᱱ" to "n",
        "ᱲ" to "r",
        "ᳳ" to "o",
        "ᱳ" to "o",
        "ᱴ" to "t",
        "ᱵ" to "b",
        "ᱶ" to "v",
        "ᱷ" to "h",
        "ᱸ" to "n",
        "ᱹ" to "",
        "ᱺ" to "",
        "ᱼ" to "-",
        "ᱽ" to "'",
        "᱾" to ".",
        " " to ", ",
        "," to ",",
        "?" to "?",
        "!" to "!",
        ":" to ":",
        ";" to ";",
        "0" to "zero",
        "1" to "mit",
        "2" to "bar",
        "3" to "pe",
        "4" to "pon",
        "5" to "morae",
        "6" to "turui",
        "7" to "eyae",
        "8" to "iral",
        "9" to "are"
    )

    fun olChikiToPiperRoman(santaliText: String): String {
        if (santaliText.isBlank()) return ""
        val builder = StringBuilder()
        for (ch in santaliText) {
            val s = ch.toString()
            builder.append(mapping[s] ?: s)
        }
        val result = builder.toString().trim()
        try {
            android.util.Log.d(TAG, "INPUT_OL_CHIKI=$santaliText")
            android.util.Log.d(TAG, "PIPER_ROMAN_TEXT=$result")
        } catch (_: Throwable) {
            println("INPUT_OL_CHIKI=$santaliText")
            println("PIPER_ROMAN_TEXT=$result")
        }
        return result
    }

    fun toRoman(santaliText: String): String = olChikiToPiperRoman(santaliText)
}
