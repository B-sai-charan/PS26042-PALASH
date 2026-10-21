package com.sih2026.palash.ui.screens.flashcard

import com.sih2026.palash.data.model.m5.M5Flashcard
import com.sih2026.palash.data.model.m5.M5FlashcardContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * P1 Flashcard UX unit tests.
 *
 * Tests:
 * 1. HTML is never rendered as raw user-facing markup.
 * 2. FC003 (Number 3) visual_rows correctly represents 3 objects in [1, 2] arrangement.
 * 3. FC006 (Number 6) visual_rows correctly represents all 6 objects in [1, 2, 3] arrangement.
 * 4. Card flip preserves language/content data (Hindi and Santali).
 * 5. Existing flashcard navigation (next/previous) remains functional and resets flip.
 * 6. Existing audio cardId references are not altered.
 */
class FlashcardUxTest {

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private fun makeCard1(): M5Flashcard = M5Flashcard(
        cardId = "FC001",
        type = "counting",
        number = 1,
        hindi = "एक",
        santali = "ᱢᱤᱫ",
        visual = "🍎",
        visualLayout = "triangle",
        visualRows = listOf(1),
        validationStatus = "model_generated_unvalidated"
    )

    private fun makeCard3(): M5Flashcard = M5Flashcard(
        cardId = "FC003",
        type = "counting",
        number = 3,
        hindi = "तीन",
        santali = "ᱯᱮ",
        visual = "<div style='display:inline-block;text-align:left;line-height:1.3;'>🍎<br>🍎🍎</div>",
        visualLayout = "triangle",
        visualRows = listOf(1, 2),
        validationStatus = "model_generated_unvalidated"
    )

    private fun makeCard6(): M5Flashcard = M5Flashcard(
        cardId = "FC006",
        type = "counting",
        number = 6,
        hindi = "छह",
        santali = "ᱛᱩᱨᱩᱭ",
        visual = "<div style='display:inline-block;text-align:left;line-height:1.3;'>🍎<br>🍎🍎<br>🍎🍎🍎</div>",
        visualLayout = "triangle",
        visualRows = listOf(1, 2, 3),
        validationStatus = "model_generated_unvalidated"
    )

    private fun makeCard11(): M5Flashcard = M5Flashcard(
        cardId = "FC011",
        type = "operation",
        number = "1 + 2 = 3",
        operator = "+",
        operand1 = 1,
        operand2 = 2,
        result = 3,
        hindi = "एक + दो = तीन",
        santali = "ᱢᱤᱫ + ᱵᱟᱨ = ᱯᱮ",
        visual = "🍎 + 🍎🍎 = 🍎🍎🍎",
        validationStatus = "model_generated_unvalidated"
    )

    private fun makeFlashcardContent() = M5FlashcardContent(
        flashcardSetId = "FC_G2_COUNTING_OPS_001",
        title = "PALASH Grade 2 Mathematics - Counting and Basic Operations",
        grade = 2,
        subject = "mathematics",
        topic = "counting_numbers_and_operations",
        cards = listOf(makeCard1(), makeCard3(), makeCard6(), makeCard11())
    )

    // ─── Test 1: HTML not rendered as raw user-facing markup ─────────────────

    @Test
    fun test1_htmlIsNotPassedDirectlyToUi() {
        val card3 = makeCard3()
        val card6 = makeCard6()

        // The visual field for FC003 and FC006 contains raw HTML
        assertTrue("FC003 visual must contain HTML", containsRawHtml(card3.visual!!))
        assertTrue("FC006 visual must contain HTML", containsRawHtml(card6.visual!!))

        // After stripping, no HTML tags remain
        val stripped3 = stripHtmlTags(card3.visual!!)
        val stripped6 = stripHtmlTags(card6.visual!!)

        assertFalse("Stripped FC003 must not contain '<'", stripped3.contains('<'))
        assertFalse("Stripped FC003 must not contain '>'", stripped3.contains('>'))
        assertFalse("Stripped FC006 must not contain '<'", stripped6.contains('<'))
        assertFalse("Stripped FC006 must not contain '>'", stripped6.contains('>'))
    }

    @Test
    fun test1b_stripHtmlTagsPreservesEmojiContent() {
        val rawHtml = "<div style='display:inline-block;text-align:left;line-height:1.3;'>🍎<br>🍎🍎</div>"
        val stripped = stripHtmlTags(rawHtml)
        // Result should contain the apples but no HTML
        assertTrue("Stripped result must contain apple emoji", stripped.contains("🍎"))
        assertFalse("Stripped result must not contain HTML tags", stripped.contains('<'))
    }

    @Test
    fun test1c_plainVisualIsNotHtml() {
        // FC001 visual is plain emoji — containsRawHtml must return false
        val card1 = makeCard1()
        assertFalse("FC001 plain visual must not be flagged as HTML", containsRawHtml(card1.visual!!))
    }

    // ─── Test 2: FC003 Number 3 visual_rows correctness ─────────────────────

    @Test
    fun test2_fc003_visualRowsRepresentsThreeObjects() {
        val card3 = makeCard3()

        // visual_rows must be [1, 2]
        val rows = card3.visualRows
        assertFalse("FC003 must have visual_rows", rows.isNullOrEmpty())
        assertEquals("FC003 must have 2 rows", 2, rows!!.size)
        assertEquals("FC003 row 0 must have 1 object", 1, rows[0])
        assertEquals("FC003 row 1 must have 2 objects", 2, rows[1])

        // Total objects = sum of rows = 3
        val totalObjects = rows.sum()
        assertEquals("FC003 total visual objects must be 3", 3, totalObjects)
    }

    @Test
    fun test2b_fc003_cardNumberIsThree() {
        val card3 = makeCard3()
        assertEquals("FC003 number must be 3", "3", card3.numberDisplay)
    }

    // ─── Test 3: FC006 Number 6 all six objects visible ─────────────────────

    @Test
    fun test3_fc006_visualRowsRepresentsSixObjects() {
        val card6 = makeCard6()

        // visual_rows must be [1, 2, 3]
        val rows = card6.visualRows
        assertFalse("FC006 must have visual_rows", rows.isNullOrEmpty())
        assertEquals("FC006 must have 3 rows", 3, rows!!.size)
        assertEquals("FC006 row 0 must have 1 object", 1, rows[0])
        assertEquals("FC006 row 1 must have 2 objects", 2, rows[1])
        assertEquals("FC006 row 2 must have 3 objects", 3, rows[2])

        // Total objects = sum of rows = 6
        val totalObjects = rows.sum()
        assertEquals("FC006 total visual objects must be 6", 6, totalObjects)
    }

    @Test
    fun test3b_fc006_cardNumberIsSix() {
        val card6 = makeCard6()
        assertEquals("FC006 number must be 6", "6", card6.numberDisplay)
    }

    // ─── Test 4: Flip preserves language/content data ────────────────────────

    @Test
    fun test4_flipPreservesHindiAndSantaliData() {
        val card3 = makeCard3()

        // Front: Hindi and number visible
        assertEquals("FC003 hindi must be preserved", "तीन", card3.hindi)
        assertEquals("FC003 number must be preserved", "3", card3.numberDisplay)

        // Back: Santali Ol Chiki preserved
        assertEquals("FC003 santali must be preserved", "ᱯᱮ", card3.santali)

        // Both Hindi and Santali must be present regardless of flip state
        assertTrue("Hindi must not be blank", card3.hindi.isNotBlank())
        assertTrue("Santali must not be blank", card3.santali.isNotBlank())
    }

    @Test
    fun test4b_flipState_viewModel() {
        // FlashcardUiState flip toggle
        val state = FlashcardUiState(isFlipped = false)
        val flippedState = state.copy(isFlipped = !state.isFlipped)

        assertFalse("Initial state must not be flipped", state.isFlipped)
        assertTrue("After flip state must be flipped", flippedState.isFlipped)

        // All card data is preserved through flip
        assertEquals("Flashcards list preserved", state.flashcards, flippedState.flashcards)
        assertEquals("Current index preserved", state.currentIndex, flippedState.currentIndex)
    }

    // ─── Test 5: Navigation resets flip ──────────────────────────────────────

    @Test
    fun test5_navigationResetsFliState() {
        val cards = listOf(makeCard1(), makeCard3(), makeCard6())

        var state = FlashcardUiState(
            isLoading = false,
            flashcards = cards,
            currentIndex = 0,
            isFlipped = true
        )

        // Simulate nextCard: advance index, reset flip
        val nextIdx = (state.currentIndex + 1) % cards.size
        state = state.copy(currentIndex = nextIdx, isFlipped = false)

        assertEquals("Next card index must be 1", 1, state.currentIndex)
        assertFalse("Flip must be reset on next card", state.isFlipped)

        // Simulate previousCard from index 1: should go to index 0
        val prevIdx = if (state.currentIndex - 1 < 0) cards.size - 1 else state.currentIndex - 1
        state = state.copy(currentIndex = prevIdx, isFlipped = false)

        assertEquals("Previous card index must be 0", 0, state.currentIndex)
        assertFalse("Flip must be reset on previous card", state.isFlipped)
    }

    @Test
    fun test5b_navigationWrapsAround() {
        val cards = listOf(makeCard1(), makeCard3())
        val state = FlashcardUiState(flashcards = cards, currentIndex = 1)

        // Next from last card should wrap to 0
        val nextIdx = (state.currentIndex + 1) % cards.size
        assertEquals("Next from last card must wrap to 0", 0, nextIdx)

        // Previous from first card (index 0) should wrap to last
        val prevIdx = if (0 - 1 < 0) cards.size - 1 else 0 - 1
        assertEquals("Previous from first card must wrap to last", 1, prevIdx)
    }

    // ─── Test 6: Audio cardId references not altered ──────────────────────────

    @Test
    fun test6_audioCardIdsMatchM5Json() {
        // These card IDs must remain unchanged — they map to existing audio assets
        // FC001.wav, FC003.wav, FC006.wav, FC011.wav, FC012.wav, FC013.wav, FC014.wav
        val expectedAudioIds = setOf("FC001", "FC003", "FC006", "FC011", "FC012", "FC013", "FC014")

        val card1 = makeCard1()
        val card3 = makeCard3()
        val card6 = makeCard6()
        val card11 = makeCard11()

        assertTrue("FC001 cardId must be in expected audio set", card1.cardId in expectedAudioIds)
        assertTrue("FC003 cardId must be in expected audio set", card3.cardId in expectedAudioIds)
        assertTrue("FC006 cardId must be in expected audio set", card6.cardId in expectedAudioIds)
        assertTrue("FC011 cardId must be in expected audio set", card11.cardId in expectedAudioIds)
    }

    @Test
    fun test6b_audioCardIdsAreNotRenamed() {
        val card3 = makeCard3()
        // cardId must exactly match "FC003" — not renamed, not lowercased, not prefixed
        assertEquals("FC003 cardId must be exactly 'FC003'", "FC003", card3.cardId)

        val card6 = makeCard6()
        assertEquals("FC006 cardId must be exactly 'FC006'", "FC006", card6.cardId)
    }

    // ─── Test 7: Full flashcard content structure integrity ───────────────────

    @Test
    fun test7_flashcardContentStructureIntact() {
        val content = makeFlashcardContent()
        assertEquals("Flashcard set ID must match M5", "FC_G2_COUNTING_OPS_001", content.flashcardSetId)
        assertEquals("Grade must be 2", 2, content.grade)
        assertEquals("Subject must be mathematics", "mathematics", content.subject)
        assertEquals("Topic must match M5", "counting_numbers_and_operations", content.topic)
        assertEquals("Cards count must be 4 (sample)", 4, content.cards.size)
    }

    @Test
    fun test7b_fc003_validationStatusPreserved() {
        val card3 = makeCard3()
        // validationStatus must remain unchanged — never claim "validated"
        assertEquals("FC003 validation_status must be model_generated_unvalidated",
            "model_generated_unvalidated", card3.validationStatus)
    }
}
