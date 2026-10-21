package com.sih2026.palash.data.demo

data class GradeItem(
    val id: String,
    val name: String,
    val description: String
)

data class SubjectItem(
    val id: String,
    val name: String,
    val topics: List<String>
)

data class WorksheetQuestion(
    val id: String,
    val grade: String,
    val subject: String,
    val topic: String,
    val hindiInstruction: String,
    val santaliInstructionNotice: String,
    val visualPrompt: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

object DemoData {
    /** Full grade catalog — includes Grade 3 for future/internal use. Do NOT use this list for MVP UI. */
    val grades = listOf(
        GradeItem("grade_1", "Grade 1", "Foundational Literacy & Early Numeracy"),
        GradeItem("grade_2", "Grade 2", "Primary Mathematics & Reading (Default Demo)"),
        GradeItem("grade_3", "Grade 3", "Basic Math & Vernacular Vocabulary") // future — not shown in SIH MVP
    )

    /**
     * SIH MVP allowed grade IDs.
     * Grade 3 is excluded from the current MVP scope.
     * Add "grade_3" here when Grade 3 M5 content is ready for integration.
     */
    val MVP_ALLOWED_GRADE_IDS = listOf("grade_1", "grade_2")

    /** Filtered grade list safe for MVP user-facing grade selection. */
    val mvpGrades = grades.filter { it.id in MVP_ALLOWED_GRADE_IDS }


    val subjects = listOf(
        SubjectItem(
            id = "math",
            name = "Mathematics",
            topics = listOf("Counting", "Numbers", "Addition")
        ),
        SubjectItem(
            id = "lang",
            name = "Language",
            topics = listOf("Letters", "Vocabulary")
        )
    )

    const val sampleHindiInput = "पाँच आम गिनो।"
    const val sampleSantaliOutputNotice = "Verified Santali translation not available"

    val sampleWorksheetQuestions = listOf(
        WorksheetQuestion(
            id = "q1",
            grade = "Grade 2",
            subject = "Mathematics",
            topic = "Counting",
            hindiInstruction = "गिनती करके सही संख्या चुनो।",
            santaliInstructionNotice = "Verified Santali translation not available",
            visualPrompt = "🍎 🍎 🍎 🍎 🍎",
            questionText = "How many apples?",
            options = listOf("3", "4", "5"),
            correctAnswer = "5"
        ),
        WorksheetQuestion(
            id = "q2",
            grade = "Grade 2",
            subject = "Mathematics",
            topic = "Counting",
            hindiInstruction = "तारों की संख्या गिनो।",
            santaliInstructionNotice = "Verified Santali translation not available",
            visualPrompt = "⭐ ⭐ ⭐",
            questionText = "How many stars?",
            options = listOf("2", "3", "4"),
            correctAnswer = "3"
        )
    )
}
