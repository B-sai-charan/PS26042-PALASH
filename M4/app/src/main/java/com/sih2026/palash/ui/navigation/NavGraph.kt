package com.sih2026.palash.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sih2026.palash.data.config.AppConfigManager
import com.sih2026.palash.model.DeviceRole
import com.sih2026.palash.ui.screens.assessment.AssessmentScreen
import com.sih2026.palash.ui.screens.flashcard.FlashcardScreen
import com.sih2026.palash.ui.screens.grade.GradeSelectionScreen
import com.sih2026.palash.ui.screens.home.HomeScreen
import com.sih2026.palash.ui.screens.lesson.LessonScreen
import com.sih2026.palash.ui.screens.pack.LanguagePackScreen
import com.sih2026.palash.ui.screens.result.TranslationResultScreen
import com.sih2026.palash.ui.screens.role.RoleSelectionScreen
import com.sih2026.palash.ui.screens.role.StudentSetupScreen
import com.sih2026.palash.ui.screens.role.TeacherPinScreen
import com.sih2026.palash.ui.screens.student.StudentHomeScreen
import com.sih2026.palash.ui.screens.subject.SubjectTopicScreen
import com.sih2026.palash.ui.screens.voice.VoiceTranslationScreen
import com.sih2026.palash.ui.screens.worksheet.WorksheetScreen
import com.sih2026.palash.ui.screens.module.ModuleSelectionScreen

object Routes {
    const val ROLE_SELECTION = "role_selection"
    const val TEACHER_PIN = "teacher_pin"
    const val STUDENT_SETUP = "student_setup"
    const val STUDENT_HOME = "student_home"
    const val HOME = "home"
    const val GRADE_SELECTION = "grade_selection"
    const val STUDENT_GRADE_SELECTION = "student_grade_selection"
    const val STUDENT_MODULE_SELECTION = "student_module_selection/{gradeName}"
    const val ASSESSMENT = "assessment"
    const val SUBJECT_SELECTION = "subject_selection/{gradeName}"
    const val LESSON = "lesson/{gradeName}/{subjectName}/{topicName}?section={sectionName}"
    const val VOICE_TRANSLATION = "voice_translation/{gradeName}/{subjectName}/{topicName}"
    const val TRANSLATION_RESULT = "translation_result?hindi={hindi}&santaliNotice={santaliNotice}&liveAudioPath={liveAudioPath}&originalAudioPath={originalAudioPath}"
    const val WORKSHEET = "worksheet"
    const val FLASHCARD = "flashcard"
    const val LANGUAGE_PACK = "language_pack"

    private fun safeEncode(value: String): String {
        return try {
            Uri.encode(value)
        } catch (_: Throwable) {
            value.replace(" ", "%20")
        }
    }

    fun buildResultRoute(
        hindi: String,
        santaliNotice: String,
        liveAudioPath: String? = null,
        originalAudioPath: String? = null
    ): String {
        val encHindi = safeEncode(hindi)
        val encSantali = safeEncode(santaliNotice)
        val encLive = if (!liveAudioPath.isNullOrBlank()) safeEncode(liveAudioPath) else ""
        val encOrig = if (!originalAudioPath.isNullOrBlank()) safeEncode(originalAudioPath) else ""
        return "translation_result?hindi=$encHindi&santaliNotice=$encSantali&liveAudioPath=$encLive&originalAudioPath=$encOrig"
    }

    fun buildLessonRoute(
        gradeName: String,
        subjectName: String,
        topicName: String,
        sectionName: String = "A"
    ): String {
        val encGrade = safeEncode(gradeName)
        val encSub = safeEncode(subjectName)
        val encTopic = safeEncode(topicName)
        val encSec = safeEncode(sectionName)
        return "lesson/$encGrade/$encSub/$encTopic?section=$encSec"
    }

    fun buildVoiceRoute(gradeName: String, subjectName: String, topicName: String): String {
        val encGrade = safeEncode(gradeName)
        val encSub = safeEncode(subjectName)
        val encTopic = safeEncode(topicName)
        return "voice_translation/$encGrade/$encSub/$encTopic"
    }

    fun buildStudentModuleRoute(gradeName: String): String {
        val encGrade = safeEncode(gradeName)
        return "student_module_selection/$encGrade"
    }

    fun buildSubjectRoute(gradeName: String): String {
        val encGrade = safeEncode(gradeName)
        return "subject_selection/$encGrade"
    }

    fun buildAssessmentRoute(gradeName: String): String {
        val encoded = safeEncode(gradeName)
        return "$ASSESSMENT?gradeName=$encoded"
    }

    fun buildWorksheetRoute(gradeName: String): String {
        val encoded = safeEncode(gradeName)
        return "$WORKSHEET?gradeName=$encoded"
    }

    fun buildFlashcardRoute(gradeName: String): String {
        val encoded = safeEncode(gradeName)
        return "$FLASHCARD?gradeName=$encoded"
    }
}

@Composable
fun PalashNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val configManager = remember { AppConfigManager(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.ROLE_SELECTION,
        modifier = modifier
    ) {
        composable(Routes.ROLE_SELECTION) {
            RoleSelectionScreen(
                onSelectTeacher = {
                    navController.navigate(Routes.TEACHER_PIN)
                },
                onSelectStudent = {
                    navController.navigate(Routes.STUDENT_GRADE_SELECTION)
                }
            )
        }

        composable(Routes.TEACHER_PIN) {
            TeacherPinScreen(
                onPinVerified = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.STUDENT_SETUP) {
            StudentSetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.STUDENT_HOME) {
                        popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.STUDENT_HOME) {
            StudentHomeScreen(
                onReconfigure = {
                    navController.navigate(Routes.ROLE_SELECTION) {
                        popUpTo(Routes.STUDENT_HOME) { inclusive = true }
                    }
                },
                onOpenLesson = { grade, subject, topic, section ->
                    navController.navigate(Routes.buildLessonRoute(grade, subject, topic, section))
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onStartLesson = {
                    navController.navigate(Routes.buildLessonRoute("Grade 2", "Mathematics", "Counting", "A"))
                },
                onManagePacks = {
                    navController.navigate(Routes.buildVoiceRoute("Grade 2", "Mathematics", "Counting"))
                },
                onSelectGrade = { selectedGrade ->
                    navController.navigate(Routes.buildLessonRoute(selectedGrade, "Mathematics", "Counting", "A"))
                },
                onBack = {
                    navController.navigate(Routes.ROLE_SELECTION) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LANGUAGE_PACK) {
            LanguagePackScreen(
                onBackToHome = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.STUDENT_GRADE_SELECTION) {
            GradeSelectionScreen(
                onContinue = { selectedGrade ->
                    navController.navigate(Routes.buildStudentModuleRoute(selectedGrade))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.STUDENT_MODULE_SELECTION,
            arguments = listOf(navArgument("gradeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            ModuleSelectionScreen(
                gradeName = gradeName,
                onSelectLessons = {
                    navController.navigate(Routes.buildLessonRoute(gradeName, "Mathematics", "Counting", "A"))
                },
                onSelectWorksheets = {
                    navController.navigate(Routes.buildWorksheetRoute(gradeName))
                },
                onSelectFlashcards = {
                    navController.navigate(Routes.buildFlashcardRoute(gradeName))
                },
                onSelectAssessments = {
                    navController.navigate(Routes.buildAssessmentRoute(gradeName))
                },
                onOpenTranslation = {
                    navController.navigate(Routes.buildVoiceRoute(gradeName, "Mathematics", "Counting"))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Routes.ASSESSMENT}?gradeName={gradeName}",
            arguments = listOf(navArgument("gradeName") { defaultValue = "Grade 2"; type = NavType.StringType })
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            AssessmentScreen(
                gradeName = gradeName,
                onBackToClassroom = {
                    navController.popBackStack()
                },
                onOpenTranslation = {
                    navController.navigate(Routes.buildVoiceRoute(gradeName, "Mathematics", "Counting"))
                }
            )
        }

        composable(Routes.GRADE_SELECTION) {
            GradeSelectionScreen(
                onContinue = { selectedGrade ->
                    navController.navigate(Routes.buildSubjectRoute(selectedGrade))
                }
            )
        }

        composable(
            route = Routes.SUBJECT_SELECTION,
            arguments = listOf(navArgument("gradeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            SubjectTopicScreen(
                gradeName = gradeName,
                onStartClassroom = { subject, topic, section ->
                    navController.navigate(Routes.buildLessonRoute(gradeName, subject, topic, section))
                }
            )
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(
                navArgument("gradeName") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType },
                navArgument("topicName") { type = NavType.StringType },
                navArgument("sectionName") {
                    type = NavType.StringType
                    defaultValue = "A"
                }
            )
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: "Mathematics"
            val topicName = backStackEntry.arguments?.getString("topicName") ?: "Counting"
            val sectionName = backStackEntry.arguments?.getString("sectionName") ?: "A"

            LessonScreen(
                gradeName = gradeName,
                subjectName = subjectName,
                topicName = topicName,
                sectionName = sectionName,
                onOpenWorksheet = {
                    navController.navigate(Routes.buildWorksheetRoute(gradeName))
                },
                onStartClassroom = {
                    navController.navigate(Routes.buildVoiceRoute(gradeName, subjectName, topicName))
                },
                onOpenTranslation = {
                    navController.navigate(Routes.buildVoiceRoute(gradeName, subjectName, topicName))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.VOICE_TRANSLATION,
            arguments = listOf(
                navArgument("gradeName") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType },
                navArgument("topicName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: "Mathematics"
            val topicName = backStackEntry.arguments?.getString("topicName") ?: "Counting"

            VoiceTranslationScreen(
                grade = gradeName,
                subject = subjectName,
                topic = topicName,
                onViewResult = { hindi, santaliNotice, liveAudioPath, originalAudioPath ->
                    navController.navigate(Routes.buildResultRoute(hindi, santaliNotice, liveAudioPath, originalAudioPath))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.TRANSLATION_RESULT,
            arguments = listOf(
                navArgument("hindi") {
                    type = NavType.StringType
                    defaultValue = "पाँच आम गिनो।"
                },
                navArgument("santaliNotice") {
                    type = NavType.StringType
                    defaultValue = "Sample translation — pending language validation"
                },
                navArgument("liveAudioPath") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("originalAudioPath") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val hindi = backStackEntry.arguments?.getString("hindi") ?: "पाँच आम गिनो।"
            val santaliNotice = backStackEntry.arguments?.getString("santaliNotice") ?: "Sample translation — pending language validation"
            val rawLive = backStackEntry.arguments?.getString("liveAudioPath") ?: ""
            val liveAudioPath = if (rawLive.isNotBlank()) Uri.decode(rawLive) else null
            val rawOrig = backStackEntry.arguments?.getString("originalAudioPath") ?: ""
            val originalAudioPath = if (rawOrig.isNotBlank()) Uri.decode(rawOrig) else null

            TranslationResultScreen(
                hindiText = hindi,
                santaliNotice = santaliNotice,
                liveAudioPath = liveAudioPath,
                originalAudioPath = originalAudioPath,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Routes.WORKSHEET}?gradeName={gradeName}",
            arguments = listOf(navArgument("gradeName") { defaultValue = "Grade 2"; type = NavType.StringType })
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            WorksheetScreen(
                gradeName = gradeName,
                onBackToClassroom = {
                    navController.popBackStack()
                },
                onOpenFlashcards = {
                    navController.navigate(Routes.buildFlashcardRoute(gradeName))
                },
                onOpenTranslation = {
                    navController.navigate(Routes.buildVoiceRoute(gradeName, "Mathematics", "Counting"))
                }
            )
        }

        composable(
            route = "${Routes.FLASHCARD}?gradeName={gradeName}",
            arguments = listOf(navArgument("gradeName") { defaultValue = "Grade 2"; type = NavType.StringType })
        ) { backStackEntry ->
            val gradeName = backStackEntry.arguments?.getString("gradeName") ?: "Grade 2"
            FlashcardScreen(
                gradeName = gradeName,
                onBackToClassroom = {
                    navController.popBackStack()
                },
                onOpenTranslation = {
                    navController.navigate(Routes.buildVoiceRoute(gradeName, "Mathematics", "Counting"))
                }
            )
        }
    }
}
