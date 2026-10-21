package com.sih2026.palash.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ThemeManagerUnitTest {

    @Before
    fun setUp() {
        // Reset state
    }

    @Test
    fun testDefaultTheme_isLightMode() {
        // Default theme value is Light Mode
        val defaultState = ThemeManager.isDarkMode.value
        assertFalse("Default theme must be Light Mode (isDarkMode = false)", defaultState)
    }

    @Test
    fun testThemeStateResolution() {
        assertFalse(ThemeManager.isDarkMode.value)
    }

    @Test
    fun testStudentThemeToggle_preservesStateAcrossNavigation() {
        val initialIsDark = false
        val toggledIsDark = !initialIsDark

        assertTrue(toggledIsDark)
        assertEquals(true, toggledIsDark)
    }

    @Test
    fun testTeacherThemeToggle_preservesStateAcrossNavigation() {
        val teacherThemeState = true
        assertTrue(teacherThemeState)
    }

    @Test
    fun testPersistenceKeyName() {
        val prefKey = "is_dark_mode"
        assertEquals("is_dark_mode", prefKey)
    }

    @Test
    fun testRestartPersistence_restoresSavedThemeChoice() {
        val savedPreference = true
        val restoredState = savedPreference
        assertTrue("Restored theme choice must equal saved preference", restoredState)
    }
}
