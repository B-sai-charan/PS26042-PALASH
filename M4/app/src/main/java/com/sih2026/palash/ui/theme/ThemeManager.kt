package com.sih2026.palash.ui.theme

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sih2026.palash.data.config.AppConfigManager

/**
 * Centralized Application Theme Manager.
 * Single source of truth for global Light / Dark theme state.
 */
object ThemeManager {
    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> get() = _isDarkMode

    fun init(context: Context) {
        val config = AppConfigManager(context)
        _isDarkMode.value = config.isDarkMode
    }

    fun toggleTheme(context: Context) {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        AppConfigManager(context).isDarkMode = next
    }

    fun setDarkMode(context: Context, dark: Boolean) {
        _isDarkMode.value = dark
        AppConfigManager(context).isDarkMode = dark
    }
}
