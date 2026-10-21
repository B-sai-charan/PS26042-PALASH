package com.sih2026.palash.ui.screens.role

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih2026.palash.data.config.AppConfigManager
import com.sih2026.palash.ui.components.M6GradeCard
import com.sih2026.palash.ui.components.PalashBrandHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentSetupScreen(
    onSetupComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val configManager = remember { AppConfigManager(context) }
    val existingConfig = remember { configManager.getStudentConfig() }

    var selectedGrade by remember { mutableIntStateOf(existingConfig.grade) }
    var selectedSection by remember { mutableStateOf(existingConfig.section) }
    var teacherIp by remember { mutableStateOf(existingConfig.teacherIp) }
    var teacherPortStr by remember { mutableStateOf(existingConfig.teacherPort.toString()) }
    val scrollState = rememberScrollState()

    val sections = listOf("A", "B")

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                PalashBrandHeader(
                    showOfflineBadge = true,
                    subtitle = "ᱪᱮᱛᱮᱫᱤᱭᱟᱹ (छात्र सेटअप)"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "छात्र डिवाइस सेटअप",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "कक्षा अनुवाद प्राप्त करने के लिए कक्षा और वर्ग का चयन करें।",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "कक्षा चुनें (ᱪᱟᱱᱟᱪ ᱪᱷᱟᱸᱴᱟᱭ ᱢᱮ)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M6GradeCard(
                        gradeNumber = 1,
                        devanagariTitle = "कक्षा 1",
                        olChikiTitle = "ᱪᱟᱱᱟᱪ ᱑",
                        subtitle = "कक्षा १",
                        isSelected = selectedGrade == 1,
                        onClick = { selectedGrade = 1 },
                        modifier = Modifier.weight(1f)
                    )

                    M6GradeCard(
                        gradeNumber = 2,
                        devanagariTitle = "कक्षा 2",
                        olChikiTitle = "ᱪᱟᱱᱟᱪ ᱒",
                        subtitle = "कक्षा २ (सक्रिय)",
                        isSelected = selectedGrade == 2,
                        onClick = { selectedGrade = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "वर्ग (Section) चुनें",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    sections.forEach { section ->
                        FilterChip(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            label = { Text("वर्ग $section", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "शिक्षक नेटवर्क पता (Local Address)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = teacherIp,
                        onValueChange = { teacherIp = it },
                        label = { Text("शिक्षक IP") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = teacherPortStr,
                        onValueChange = { teacherPortStr = it },
                        label = { Text("पोर्ट") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val parsedPort = teacherPortStr.toIntOrNull() ?: 8080
                        configManager.saveStudentConfig(
                            grade = selectedGrade,
                            section = selectedSection,
                            teacherIp = teacherIp,
                            teacherPort = parsedPort
                        )
                        onSetupComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "सहेजें और छात्र मोड शुरू करें",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "भूमिका चयन पर वापस जाएं",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
