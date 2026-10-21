package com.sih2026.palash.classroom

import com.google.gson.annotations.SerializedName

/**
 * Enumeration of supported classroom message types.
 * Standard serialized format: "CLASSROOM_TRANSLATION".
 */
enum class ClassroomMessageType {
    @SerializedName("CLASSROOM_TRANSLATION")
    CLASSROOM_TRANSLATION
}
