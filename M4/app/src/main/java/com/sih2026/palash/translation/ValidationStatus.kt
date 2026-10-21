package com.sih2026.palash.translation

/**
 * Validation status model for domain translations in PALASH.
 * Only TEACHER_VERIFIED and OFFICIAL_SOURCE translations are permitted for production return.
 */
enum class ValidationStatus {
    UNVERIFIED,
    TEACHER_VERIFIED,
    OFFICIAL_SOURCE,
    REJECTED
}
