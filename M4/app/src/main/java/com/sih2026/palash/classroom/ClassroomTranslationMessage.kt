package com.sih2026.palash.classroom

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Strongly typed data model representing a CLASSROOM_TRANSLATION message broadcast from Teacher -> Student.
 * Contains recognized Hindi text, translated Santali text, and optional local audioId reference.
 */
data class ClassroomTranslationMessage(
    @SerializedName("messageId")
    val messageId: String,

    @SerializedName("type")
    val type: ClassroomMessageType = ClassroomMessageType.CLASSROOM_TRANSLATION,

    @SerializedName("grade")
    val grade: Int,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("lessonId")
    val lessonId: Int? = null,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("hindiText")
    val hindiText: String? = null,

    @SerializedName("santaliText")
    val santaliText: String,

    @SerializedName("audioId")
    val audioId: String? = null
) {
    fun toJson(gson: Gson = Gson()): String {
        return gson.toJson(this)
    }

    companion object {
        fun fromJson(json: String, gson: Gson = Gson()): ClassroomTranslationMessage? {
            return try {
                gson.fromJson(json, ClassroomTranslationMessage::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
