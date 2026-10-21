package com.sih2026.palash.data.config

import android.content.Context
import android.content.SharedPreferences
import com.sih2026.palash.model.DeviceRole
import com.sih2026.palash.model.StudentConfig

/**
 * Clean persistent configuration manager using SharedPreferences.
 * Stores device role, assigned student grade/section, and prototype teacher PIN setting across app restarts.
 */
class AppConfigManager(context: Context) {

    companion object {
        private const val PREF_NAME = "palash_app_config"
        private const val KEY_IS_CONFIGURED = "is_configured"
        private const val KEY_DEVICE_ROLE = "device_role"
        private const val KEY_STUDENT_GRADE = "student_grade"
        private const val KEY_STUDENT_SECTION = "student_section"
        private const val KEY_TEACHER_IP = "teacher_ip"
        private const val KEY_TEACHER_PORT = "teacher_port"
        private const val KEY_TEACHER_PIN = "teacher_pin"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"

        /** Fixed 6-digit numeric PIN. */
        const val DEFAULT_TEACHER_PIN = "501301"
        const val DEFAULT_TEACHER_IP = "192.168.43.1"
        const val DEFAULT_TEACHER_PORT = 8080
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()

    var isConfigured: Boolean
        get() = prefs.getBoolean(KEY_IS_CONFIGURED, false)
        private set(value) = prefs.edit().putBoolean(KEY_IS_CONFIGURED, value).apply()

    fun getDeviceRole(): DeviceRole? {
        val roleStr = prefs.getString(KEY_DEVICE_ROLE, null) ?: return null
        return try {
            DeviceRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun setTeacherRole() {
        prefs.edit()
            .putString(KEY_DEVICE_ROLE, DeviceRole.TEACHER.name)
            .putBoolean(KEY_IS_CONFIGURED, true)
            .apply()
    }

    fun saveStudentConfig(
        grade: Int,
        section: String,
        teacherIp: String = DEFAULT_TEACHER_IP,
        teacherPort: Int = DEFAULT_TEACHER_PORT
    ) {
        val cleanSection = section.trim().ifEmpty { "A" }
        val cleanIp = teacherIp.trim().ifEmpty { DEFAULT_TEACHER_IP }
        prefs.edit()
            .putString(KEY_DEVICE_ROLE, DeviceRole.STUDENT.name)
            .putInt(KEY_STUDENT_GRADE, grade)
            .putString(KEY_STUDENT_SECTION, cleanSection)
            .putString(KEY_TEACHER_IP, cleanIp)
            .putInt(KEY_TEACHER_PORT, if (teacherPort > 0) teacherPort else DEFAULT_TEACHER_PORT)
            .putBoolean(KEY_IS_CONFIGURED, true)
            .apply()
    }

    fun getStudentConfig(): StudentConfig {
        val grade = prefs.getInt(KEY_STUDENT_GRADE, 2)
        val section = prefs.getString(KEY_STUDENT_SECTION, "A") ?: "A"
        val ip = prefs.getString(KEY_TEACHER_IP, DEFAULT_TEACHER_IP) ?: DEFAULT_TEACHER_IP
        val port = prefs.getInt(KEY_TEACHER_PORT, DEFAULT_TEACHER_PORT)
        return StudentConfig(grade = grade, section = section, teacherIp = ip, teacherPort = port)
    }

    fun getTeacherPin(): String {
        return DEFAULT_TEACHER_PIN
    }

    fun verifyTeacherPin(pinInput: String): Boolean {
        val clean = pinInput.trim()
        if (clean.length != 6 || !clean.all { it.isDigit() }) {
            return false
        }
        return clean == DEFAULT_TEACHER_PIN
    }

    fun resetConfig() {
        prefs.edit().clear().apply()
    }
}
