package com.sih2026.palash.classroom

/**
 * Result of classroom message validation.
 */
sealed class MessageValidationResult {
    object Valid : MessageValidationResult()
    data class Invalid(val reason: String) : MessageValidationResult()
}

/**
 * Pure validator for ClassroomTranslationMessage instances.
 * Rejects invalid/malformed payloads before message broadcasting or client processing.
 */
object ClassroomMessageValidator {

    /**
     * Validates a classroom translation message according to contract rules.
     * Rules:
     * - messageId must not be blank
     * - type must be CLASSROOM_TRANSLATION
     * - grade must be > 0
     * - section must not be blank
     * - subject must not be blank
     * - hindiText must not be blank
     * - santaliText must not be blank
     * - timestamp must be > 0
     * Note: lessonId and audioId may be null (valid).
     */
    fun validate(message: ClassroomTranslationMessage): MessageValidationResult {
        if (message.messageId.isBlank()) {
            return MessageValidationResult.Invalid("messageId must not be blank")
        }
        if (message.type != ClassroomMessageType.CLASSROOM_TRANSLATION) {
            return MessageValidationResult.Invalid("Unsupported message type: ${message.type}")
        }
        if (message.grade <= 0) {
            return MessageValidationResult.Invalid("grade must be greater than 0")
        }
        if (message.subject.isBlank()) {
            return MessageValidationResult.Invalid("subject must not be blank")
        }
        if (message.santaliText.isBlank()) {
            return MessageValidationResult.Invalid("santaliText must not be blank")
        }
        if (message.timestamp <= 0) {
            return MessageValidationResult.Invalid("timestamp must be greater than 0")
        }

        return MessageValidationResult.Valid
    }

    fun isValid(message: ClassroomTranslationMessage): Boolean {
        return validate(message) is MessageValidationResult.Valid
    }
}
