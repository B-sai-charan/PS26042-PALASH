package com.sih2026.palash.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sih2026.palash.data.model.m5.*
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Offline Asset Loader Repository for M5 Grade 2 content.
 * Safely loads and deserializes JSON assets without network, external dependencies, or Grade 3 leaks.
 */
class M5ContentRepository(
    private val context: Context? = null,
    private val gson: Gson = Gson()
) {

    companion object {
        const val GRADE_2_DIR = "content/grade_2"
        const val LESSON_G2_FILE = "lesson_g2_counting.json"
        const val WORKSHEET_G2_FILE = "worksheet_g2_counting.json"
        const val FLASHCARDS_G2_FILE = "flashcards_g2_counting.json"
        const val ASSESSMENTS_G2_FILE = "assessments_g2_counting.json"

        const val GRADE_3_DIR = "content/grade_3"
        const val LESSON_G3_FILE = "lesson_g3_counting.json"
        const val WORKSHEET_G3_FILE = "worksheet_g3_counting.json"
        const val FLASHCARDS_G3_FILE = "flashcards_g3_counting.json"
        const val ASSESSMENTS_G3_FILE = "assessments_g3_counting.json"
    }

    // --- JSON Asset Loaders ---

    fun loadJsonFromStream(inputStream: InputStream): String {
        return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }

    fun loadJsonFromAsset(assetPath: String): Result<String> {
        if (context == null) {
            return Result.failure(IllegalStateException("Android Context is required to load assets"))
        }
        return try {
            val stream = context.assets.open(assetPath)
            stream.use { Result.success(loadJsonFromStream(it)) }
        } catch (e: FileNotFoundException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Deserialization & Validation ---

    fun parseLesson(jsonString: String, targetGrade: Int = 2): Result<M5LessonContent> {
        return try {
            val lesson = gson.fromJson(jsonString, M5LessonContent::class.java)
                ?: return Result.failure(IllegalArgumentException("Parsed lesson content is null"))

            val lessonId = lesson.lessonId as String?
            if (lessonId.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Missing required field: lesson_id"))
            }
            if (lesson.grade != targetGrade) {
                return Result.failure(IllegalArgumentException("M5ContentRepository is scoped to Grade $targetGrade, found grade: ${lesson.grade}"))
            }
            val sections = lesson.sections as List<*>?
            if (sections.isNullOrEmpty()) {
                return Result.failure(IllegalArgumentException("Lesson must contain non-empty sections"))
            }
            Result.success(lesson)
        } catch (e: JsonSyntaxException) {
            Result.failure(IllegalArgumentException("Malformed lesson JSON: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseWorksheet(jsonString: String, targetGrade: Int = 2): Result<M5WorksheetContent> {
        return try {
            val worksheet = gson.fromJson(jsonString, M5WorksheetContent::class.java)
                ?: return Result.failure(IllegalArgumentException("Parsed worksheet content is null"))

            val worksheetId = worksheet.worksheetId as String?
            if (worksheetId.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Missing required field: worksheet_id"))
            }
            if (worksheet.grade != targetGrade) {
                return Result.failure(IllegalArgumentException("M5ContentRepository is scoped to Grade $targetGrade, found grade: ${worksheet.grade}"))
            }
            val questions = worksheet.questions as List<*>?
            if (questions.isNullOrEmpty()) {
                return Result.failure(IllegalArgumentException("Worksheet must contain non-empty questions"))
            }
            Result.success(worksheet)
        } catch (e: JsonSyntaxException) {
            Result.failure(IllegalArgumentException("Malformed worksheet JSON: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseFlashcards(jsonString: String, targetGrade: Int = 2): Result<M5FlashcardContent> {
        return try {
            val flashcards = gson.fromJson(jsonString, M5FlashcardContent::class.java)
                ?: return Result.failure(IllegalArgumentException("Parsed flashcards content is null"))

            val flashcardSetId = flashcards.flashcardSetId as String?
            if (flashcardSetId.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Missing required field: flashcard_set_id"))
            }
            if (flashcards.grade != targetGrade) {
                return Result.failure(IllegalArgumentException("M5ContentRepository is scoped to Grade $targetGrade, found grade: ${flashcards.grade}"))
            }
            val cards = flashcards.cards as List<*>?
            if (cards.isNullOrEmpty()) {
                return Result.failure(IllegalArgumentException("Flashcards must contain non-empty cards"))
            }
            Result.success(flashcards)
        } catch (e: JsonSyntaxException) {
            Result.failure(IllegalArgumentException("Malformed flashcards JSON: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseAssessment(jsonString: String, targetGrade: Int = 2): Result<M5AssessmentContent> {
        return try {
            val assessment = gson.fromJson(jsonString, M5AssessmentContent::class.java)
                ?: return Result.failure(IllegalArgumentException("Parsed assessment content is null"))

            val assessmentId = assessment.assessmentId as String?
            if (assessmentId.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Missing required field: assessment_id"))
            }
            if (assessment.grade != targetGrade) {
                return Result.failure(IllegalArgumentException("M5ContentRepository is scoped to Grade $targetGrade, found grade: ${assessment.grade}"))
            }
            val questions = assessment.questions as List<*>?
            if (questions.isNullOrEmpty()) {
                return Result.failure(IllegalArgumentException("Assessment must contain non-empty questions"))
            }
            Result.success(assessment)
        } catch (e: JsonSyntaxException) {
            Result.failure(IllegalArgumentException("Malformed assessment JSON: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- High-Level Asset Loaders ---

    fun loadGrade2Lesson(): Result<M5LessonContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_2_DIR/$LESSON_G2_FILE")
        return jsonResult.mapCatching { parseLesson(it, 2).getOrThrow() }
    }

    fun loadGrade2Worksheet(): Result<M5WorksheetContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_2_DIR/$WORKSHEET_G2_FILE")
        return jsonResult.mapCatching { parseWorksheet(it, 2).getOrThrow() }
    }

    fun loadGrade2Flashcards(): Result<M5FlashcardContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_2_DIR/$FLASHCARDS_G2_FILE")
        return jsonResult.mapCatching { parseFlashcards(it, 2).getOrThrow() }
    }

    fun loadGrade2Assessment(): Result<M5AssessmentContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_2_DIR/$ASSESSMENTS_G2_FILE")
        return jsonResult.mapCatching { parseAssessment(it, 2).getOrThrow() }
    }

    fun loadGrade3Lesson(): Result<M5LessonContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_3_DIR/$LESSON_G3_FILE")
        return jsonResult.mapCatching { parseLesson(it, 3).getOrThrow() }
    }

    fun loadGrade3Worksheet(): Result<M5WorksheetContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_3_DIR/$WORKSHEET_G3_FILE")
        return jsonResult.mapCatching { parseWorksheet(it, 3).getOrThrow() }
    }

    fun loadGrade3Flashcards(): Result<M5FlashcardContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_3_DIR/$FLASHCARDS_G3_FILE")
        return jsonResult.mapCatching { parseFlashcards(it, 3).getOrThrow() }
    }

    fun loadGrade3Assessment(): Result<M5AssessmentContent> {
        val jsonResult = loadJsonFromAsset("$GRADE_3_DIR/$ASSESSMENTS_G3_FILE")
        return jsonResult.mapCatching { parseAssessment(it, 3).getOrThrow() }
    }
}
