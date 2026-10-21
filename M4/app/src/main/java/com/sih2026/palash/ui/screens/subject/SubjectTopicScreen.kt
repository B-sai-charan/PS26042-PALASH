package com.sih2026.palash.ui.screens.subject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.localization.teacherStringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectTopicScreen(
    gradeName: String,
    onStartClassroom: (selectedSubject: String, selectedTopic: String, selectedSection: String) -> Unit,
    viewModel: SubjectTopicViewModel = viewModel()
) {
    LaunchedEffect(gradeName) {
        if (gradeName.isNotBlank()) {
            viewModel.setGrade(gradeName)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    val displayGradeName = when (uiState.gradeName) {
        "Grade 1" -> teacherStringResource(R.string.grade_1_name)
        "Grade 2" -> teacherStringResource(R.string.grade_2_name)
        // Grade 3 not in MVP scope; falls through to else which returns raw name
        else -> uiState.gradeName
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "पलाश",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OfflineStatusIndicator(status = NetworkStatus.OFFLINE)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = teacherStringResource(R.string.subject_selected_grade, displayGradeName),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = teacherStringResource(R.string.subject_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                uiState.subjects.forEach { subject ->
                    val isSubjectSelected = subject.id == uiState.selectedSubjectId
                    val displaySubjectName = when (subject.id) {
                        "math" -> teacherStringResource(R.string.subject_math)
                        "lang" -> teacherStringResource(R.string.subject_language)
                        else -> subject.name
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { viewModel.selectSubject(subject.id) },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (isSubjectSelected) 2.dp else 1.dp,
                            color = if (isSubjectSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSubjectSelected)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = displaySubjectName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubjectSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subject.topics.forEach { topic ->
                                    val isTopicSelected = isSubjectSelected && topic == uiState.selectedTopic

                                    val displayTopicName = when (topic) {
                                        "Counting" -> teacherStringResource(R.string.topic_counting)
                                        "Numbers" -> teacherStringResource(R.string.topic_numbers)
                                        "Addition" -> teacherStringResource(R.string.topic_addition)
                                        "Letters" -> teacherStringResource(R.string.topic_letters)
                                        "Vocabulary" -> teacherStringResource(R.string.topic_vocabulary)
                                        else -> topic
                                    }

                                    FilterChip(
                                        selected = isTopicSelected,
                                        onClick = {
                                            viewModel.selectSubject(subject.id)
                                            viewModel.selectTopic(topic)
                                        },
                                        label = {
                                            Text(
                                                text = displayTopicName,
                                                fontSize = 14.sp,
                                                fontWeight = if (isTopicSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = teacherStringResource(R.string.subject_target_section),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("A", "B").forEach { sec ->
                        val isSecSelected = sec.equals(uiState.selectedSection, ignoreCase = true)
                        FilterChip(
                            selected = isSecSelected,
                            onClick = { viewModel.selectSection(sec) },
                            label = { Text(teacherStringResource(R.string.subject_section_format, sec), fontSize = 14.sp, fontWeight = if (isSecSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val subjectName = uiState.subjects.find { it.id == uiState.selectedSubjectId }?.name ?: "Mathematics"
                    onStartClassroom(subjectName, uiState.selectedTopic, uiState.selectedSection)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = teacherStringResource(R.string.subject_start_classroom),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

