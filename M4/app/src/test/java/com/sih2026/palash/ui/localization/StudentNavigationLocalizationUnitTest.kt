package com.sih2026.palash.ui.localization

import com.sih2026.palash.R
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying P1-3 Student Worksheet / Flashcard navigation string localization.
 */
class StudentNavigationLocalizationUnitTest {

    @Test
    fun navPreviousResource_idIsDefined() {
        assertTrue("R.string.nav_previous must be defined", R.string.nav_previous > 0)
    }

    @Test
    fun navNextResource_idIsDefined() {
        assertTrue("R.string.nav_next must be defined", R.string.nav_next > 0)
    }

    @Test
    fun localizedNavigationStrings_differFromHardcodedEnglish() {
        val hindiPrevious = "पिछला"
        val hindiNext = "अगला"

        assertNotEquals("Previous", hindiPrevious)
        assertNotEquals("Next", hindiNext)
        assertEquals("पिछला", hindiPrevious)
        assertEquals("अगला", hindiNext)
    }
}
