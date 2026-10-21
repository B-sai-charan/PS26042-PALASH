package com.sih2026.palash.data.adapter

import com.google.gson.Gson
import com.sih2026.palash.data.local.entity.FlashcardEntity
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.local.entity.WorksheetEntity
import com.sih2026.palash.data.model.m5.M5FlashcardContent
import com.sih2026.palash.data.model.m5.M5LessonContent
import com.sih2026.palash.data.model.m5.M5WorksheetContent

/**
 * Adapter utility mapping M5 Grade 2 content models to existing Room Database entities.
 * Preserves existing Room schema without structural table redesign.
 */
object M5RoomAdapter {

    fun toLessonEntity(lesson: M5LessonContent, gson: Gson = Gson()): LessonEntity {
        return LessonEntity(
            id = lesson.lessonId,
            grade = lesson.grade.toString(),
            subject = lesson.subject,
            topic = lesson.topic,
            title = lesson.title,
            languageCode = lesson.targetLanguage ?: "sat_Olck",
            contentJson = gson.toJson(lesson)
        )
    }

    fun toWorksheetEntity(worksheet: M5WorksheetContent, gson: Gson = Gson()): WorksheetEntity {
        return WorksheetEntity(
            id = worksheet.worksheetId,
            grade = worksheet.grade.toString(),
            subject = worksheet.subject,
            topic = worksheet.topic,
            title = worksheet.title,
            sourceLanguage = worksheet.sourceLanguage ?: "hin_Deva",
            targetLanguage = worksheet.targetLanguage ?: "sat_Olck",
            contentJson = gson.toJson(worksheet),
            version = "1.0.0"
        )
    }

    fun toFlashcardEntities(flashcards: M5FlashcardContent): List<FlashcardEntity> {
        return flashcards.cards.map { card ->
            FlashcardEntity(
                id = card.cardId,
                lessonId = 1,
                frontText = card.hindi,
                backText = card.santali,
                language = flashcards.targetLanguage ?: "sat_Olck",
                version = 1
            )
        }
    }
}
