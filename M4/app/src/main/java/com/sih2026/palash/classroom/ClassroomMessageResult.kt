package com.sih2026.palash.classroom

/**
 * Clean result classification for classroom translation message evaluation.
 * Exposes explicit state transitions to the application architecture and M6 UI components.
 */
enum class ClassroomMessageResult {
    /** Target Grade and Section match, payload is valid, message is new. */
    ACCEPTED,

    /** Message targeted a different Grade than assigned to the student. */
    WRONG_GRADE,

    /** Message targeted a different Section than assigned to the student. */
    WRONG_SECTION,

    /** Message payload failed contract validation (e.g. blank text or timestamp). */
    INVALID_MESSAGE,

    /** Message has already been received and displayed previously (same messageId). */
    DUPLICATE,

    /** No message was returned by the classroom server. */
    NO_MESSAGE
}
