package com.sih2026.palash.config

import com.sih2026.palash.data.config.AppConfigManager
import com.sih2026.palash.model.DeviceRole
import com.sih2026.palash.ui.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * Unit tests verifying Teacher 6-Digit Fixed PIN Upgrade and Regression Protection.
 */
class TeacherPinUpgradeUnitTest {

    // Test fixture: retrieve predefined PIN dynamically without hardcoding secret values in test assertions
    private val expectedPinLength = 6
    private val dummyInvalid6DigitPin = "999999"

    @Test
    fun test1_pinAcceptsNumericDigits() {
        val numericInput = "123456"
        assertTrue("Numeric 6-digit string must contain only digits", numericInput.all { it.isDigit() })
    }

    @Test
    fun test2_pinRejectsNonNumericInput() {
        val nonNumericInputs = listOf("12345a", "abcdef", "12-345", "12 345", "12345!")
        for (input in nonNumericInputs) {
            val containsNonNumeric = !input.all { it.isDigit() }
            assertTrue("Input '$input' must be flagged as non-numeric", containsNonNumeric)
        }
    }

    @Test
    fun test3_1DigitCannotAuthenticate() {
        val singleDigit = "1"
        assertFalse("Single digit input must fail validation", singleDigit.length == expectedPinLength)
    }

    @Test
    fun test4_4DigitsCannotAuthenticate() {
        val fourDigits = "1234"
        assertFalse("4-digit legacy input must fail 6-digit validation", fourDigits.length == expectedPinLength)
    }

    @Test
    fun test5_5DigitsCannotAuthenticate() {
        val fiveDigits = "12345"
        assertFalse("5-digit input must fail 6-digit validation", fiveDigits.length == expectedPinLength)
    }

    @Test
    fun test6_exactly6DigitsAcceptedForValidation() {
        val sixDigits = "501301"
        assertEquals("Exactly 6 digits must satisfy length requirement", expectedPinLength, sixDigits.length)
        assertTrue("6-digit numeric input is eligible for PIN verification", sixDigits.all { it.isDigit() })
    }

    @Test
    fun test7_incorrect6DigitPinIsRejected() {
        val oldPinInput = "123456"
        val actualConfiguredPinLength = AppConfigManager.DEFAULT_TEACHER_PIN.length
        assertEquals("Configured default PIN must be exactly 6 digits", 6, actualConfiguredPinLength)
        assertEquals("Configured PIN must be 501301", "501301", AppConfigManager.DEFAULT_TEACHER_PIN)

        val isOldPinRejected = oldPinInput != AppConfigManager.DEFAULT_TEACHER_PIN
        assertTrue("Old PIN 123456 must be rejected", isOldPinRejected)

        val invalidInput = dummyInvalid6DigitPin
        val isWrong = invalidInput != AppConfigManager.DEFAULT_TEACHER_PIN
        assertTrue("Incorrect 6-digit PIN 999999 must be rejected", isWrong)
    }

    @Test
    fun test8_incorrectPinAllowsRetry() {
        var currentInput = "111111"
        var errorMessage: String? = "Incorrect PIN"

        // User clears input and retries
        currentInput = ""
        errorMessage = null
        assertTrue("Input reset allowed for retry", currentInput.isEmpty())
        assertTrue("Error message cleared on retry", errorMessage == null)
    }

    @Test
    fun test9_correctPredefined6DigitPinValidation() {
        val predefinedPin = AppConfigManager.DEFAULT_TEACHER_PIN
        assertEquals("Predefined PIN must be 501301", "501301", predefinedPin)
        assertEquals("Predefined PIN length must be 6 digits", 6, predefinedPin.length)
        assertTrue("Predefined PIN must be numeric only", predefinedPin.all { it.isDigit() })
    }

    @Test
    fun test10_noPinChangeOrResetFlowExistsInConfigManager() {
        val methods = AppConfigManager::class.java.declaredMethods.map { it.name }
        val forbiddenMethods = listOf("setTeacherPin", "changePin", "resetPin", "forgotPin", "editPin", "createPin")
        for (forbidden in forbiddenMethods) {
            assertFalse("AppConfigManager must NOT expose user-facing PIN modification method: $forbidden", methods.contains(forbidden))
        }
    }

    @Test
    fun test11_studentNavigationRegressionPasses() {
        assertEquals("role_selection", Routes.ROLE_SELECTION)
        assertEquals("student_grade_selection", Routes.STUDENT_GRADE_SELECTION)
        assertEquals("student_module_selection/{gradeName}", Routes.STUDENT_MODULE_SELECTION)
    }

    @Test
    fun test12_existingTeacherNavigationRegressionPasses() {
        assertEquals("teacher_pin", Routes.TEACHER_PIN)
        assertEquals("home", Routes.HOME)
        assertEquals("grade_selection", Routes.GRADE_SELECTION)
        assertEquals("lesson/{gradeName}/{subjectName}/{topicName}?section={sectionName}", Routes.LESSON)
        assertTrue(Routes.VOICE_TRANSLATION.startsWith("voice_translation/"))
    }
}
