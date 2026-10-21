package com.sih2026.palash.model

/**
 * In-memory model representing an active Teacher classroom session context.
 */
data class TeacherSession(
    val grade: Int = 2,
    val subject: String = "Mathematics",
    val section: String = "A"
)

/**
 * Persistent configuration for a Student device.
 */
data class StudentConfig(
    val grade: Int = 2,
    val section: String = "A",
    val teacherIp: String = "192.168.43.1",
    val teacherPort: Int = 8080
)
