package com.sih2026.palash.model

/**
 * Operating mode for PALASH Android application.
 * - TEACHER: Performs live ASR, M1 translation, and audio selection.
 * - STUDENT: Receives broadcast translation messages for assigned section.
 */
enum class DeviceRole {
    TEACHER,
    STUDENT
}
