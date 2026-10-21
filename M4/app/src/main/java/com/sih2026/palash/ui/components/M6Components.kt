package com.sih2026.palash.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih2026.palash.ui.theme.Grade1Color
import com.sih2026.palash.ui.theme.Grade2Color
import com.sih2026.palash.ui.theme.Grade3Color
import com.sih2026.palash.ui.theme.KeypadBg
import com.sih2026.palash.ui.theme.PalashTerracotta
import com.sih2026.palash.ui.theme.StudentOrange
import com.sih2026.palash.ui.theme.StudentOrangeBorder
import com.sih2026.palash.ui.theme.StudentOrangeContainer
import com.sih2026.palash.ui.theme.TeacherGreen
import com.sih2026.palash.ui.theme.TeacherGreenBorder
import com.sih2026.palash.ui.theme.TeacherGreenContainer

/**
 * Top M6 Brand Header Component with Logo Emblem, Devanagari & Ol Chiki Title, and Offline Badge.
 */
@Composable
fun PalashBrandHeader(
    modifier: Modifier = Modifier,
    showOfflineBadge: Boolean = true,
    subtitle: String? = "भाषाओं को जोड़ें, सीखने का उजाला फैलाएं"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showOfflineBadge) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFCC80))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ऑफ़लाइन काम करता है",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Emblem & Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            M6PalashLogo(size = 68.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "पलाश",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = PalashTerracotta,
                letterSpacing = 1.sp
            )

            Text(
                text = "ᱯᱚᱞᱟᱥ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}

/**
 * Reusable M6 Role Selection Card (Student vs Teacher).
 */
@Composable
fun M6RoleCard(
    roleTitle: String,
    olChikiSubtitle: String,
    isTeacher: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.sih2026.palash.ui.theme.ThemeManager.isDarkMode.value

    val containerBg = when {
        isDark -> if (isTeacher) Color(0xFF1E3320) else Color(0xFF3B261D)
        isTeacher -> TeacherGreenContainer
        else -> StudentOrangeContainer
    }
    val borderColor = when {
        isDark -> if (isTeacher) Color(0xFF388E3C) else Color(0xFFE65100)
        isTeacher -> TeacherGreenBorder
        else -> StudentOrangeBorder
    }
    val buttonBg = if (isTeacher) TeacherGreen else StudentOrange
    val titleTextColor = when {
        isDark -> if (isTeacher) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
        isTeacher -> Color(0xFF1B5E20)
        else -> Color(0xFFE65100)
    }
    val iconEmoji = if (isTeacher) "👩‍🏫" else "🧑‍🎓"
    val descriptionText = if (isTeacher) "कक्षा नियंत्रण एवं उपकरण" else "अभ्यास, पाठ एवं मूल्यांकन"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = iconEmoji, fontSize = 64.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = roleTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )

            Text(
                text = olChikiSubtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) titleTextColor.copy(alpha = 0.9f) else (if (isTeacher) Color(0xFF2E7D32) else Color(0xFFF57C00))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = descriptionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFD4CDD8) else Color(0xFF616161),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = buttonBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$roleTitle ($olChikiSubtitle)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun M6GradeCard(
    gradeNumber: Int,
    devanagariTitle: String,
    olChikiTitle: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.sih2026.palash.ui.theme.ThemeManager.isDarkMode.value
    val isAvailable = gradeNumber == 1 || gradeNumber == 2

    val cardBg = when {
        !isAvailable -> if (isDark) Color(0xFF1E2225) else Color(0xFFECEFF1)
        isSelected -> if (isDark) Color(0xFF004D40) else Grade2Color
        else -> if (isDark) Color(0xFF1F2933) else Color(0xFFE3F2FD)
    }

    val borderColor = when {
        !isAvailable -> if (isDark) Color(0xFF37474F) else Color(0xFFCFD8DC)
        isSelected -> Color(0xFFFF9800)
        else -> if (isDark) Color(0xFF334E68) else Color(0xFF90CAF9)
    }

    val textColor = when {
        !isAvailable -> if (isDark) Color(0xFF78909C) else Color(0xFF78909C)
        isSelected -> Color.White
        else -> if (isDark) Color(0xFF9FB3C8) else Color(0xFF0D47A1)
    }

    val digitDevanagari = when (gradeNumber) {
        1 -> "१"
        2 -> "२"
        3 -> "३"
        else -> "$gradeNumber"
    }

    val digitOlChiki = when (gradeNumber) {
        1 -> "᱑"
        2 -> "᱒"
        3 -> "᱓"
        else -> "$gradeNumber"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        border = BorderStroke(if (isSelected) 3.dp else 1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = devanagariTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4CAF50)
                    ) {
                        Text(
                            text = " ✓ ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (gradeNumber == 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFFFCC80))
                    ) {
                        Text(
                            text = "चुनें",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = olChikiTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = digitDevanagari,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = digitOlChiki,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Reusable M6 Student Module Selection Card.
 */
@Composable
fun M6StudentModuleCard(
    iconText: String,
    titleHindi: String,
    titleSantali: String,
    subtitle: String,
    containerBg: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.sih2026.palash.ui.theme.ThemeManager.isDarkMode.value

    val effectiveBg = if (isDark) {
        when (iconText) {
            "🎴" -> Color(0xFF381E3D)
            "📝" -> Color(0xFF193242)
            "🖊️" -> Color(0xFF3E1A2B)
            "📚" -> Color(0xFF3E2818)
            else -> Color(0xFF2B2830)
        }
    } else containerBg

    val effectiveBorder = if (isDark) {
        when (iconText) {
            "🎴" -> Color(0xFF8E24AA)
            "📝" -> Color(0xFF0288D1)
            "🖊️" -> Color(0xFFC2185B)
            "📚" -> Color(0xFFF57C00)
            else -> Color(0xFF4A4550)
        }
    } else borderColor

    val effectiveText = if (isDark) {
        when (iconText) {
            "🎴" -> Color(0xFFE1BEE7)
            "📝" -> Color(0xFFB3E5FC)
            "🖊️" -> Color(0xFFF8BBD0)
            "📚" -> Color(0xFFFFE0B2)
            else -> Color(0xFFF0EAE1)
        }
    } else textColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = effectiveBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 4.dp else 2.dp),
        border = BorderStroke(1.5.dp, effectiveBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = effectiveBorder.copy(alpha = 0.25f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = iconText, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = titleHindi,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = effectiveText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$titleSantali • $subtitle",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = effectiveText.copy(alpha = 0.85f)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = effectiveText.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "❯",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = effectiveText
                    )
                }
            }
        }
    }
}

/**
 * Reusable M6 Teacher PIN Keypad Component.
 */
@Composable
fun M6PinKeypad(
    pinLength: Int,
    maxPinLength: Int = 6,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Dot Indicators (6 positions)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until maxPinLength) {
                val filled = i < pinLength
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (filled) PalashTerracotta else Color(0xFFE0E0E0))
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3x4 Numeric Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "BACKSPACE")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else if (key == "BACKSPACE") {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { onBackspaceClick() },
                            shape = CircleShape,
                            color = KeypadBg.copy(alpha = 0.7f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Backspace",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { onDigitClick(key) },
                            shape = CircleShape,
                            color = KeypadBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Concise Provenance Warning Badge for M5 Content.
 */
@Composable
fun M6UnverifiedBadge(
    modifier: Modifier = Modifier,
    text: String = "⚠️ असत्यापित सामग्री"
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Reusable Student Translation Action Button for top-right placement on Student screens.
 * Routes Student to the Voice Translation (ASR) screen.
 * Must NOT be used on Teacher screens.
 */
@Composable
fun StudentTranslationButton(
    onOpenTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onOpenTranslation,
        modifier = modifier
            .semantics {
                testTag = "student_translation_button"
                contentDescription = "अनुवाद"
            },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TeacherGreen)
    ) {
        Text(
            text = "文A  अनुवाद / ᱟᱱᱩᱵᱟᱫᱽ >",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TeacherGreen
        )
    }
}

/**
 * Global Dark / Light Theme Toggle Button.
 * Available on every app screen (Student & Teacher).
 * Compact, subtle top-right utility control.
 */
@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDark = com.sih2026.palash.ui.theme.ThemeManager.isDarkMode.value

    Surface(
        modifier = modifier
            .semantics {
                testTag = "theme_toggle_button"
                contentDescription = if (isDark) "लाइट थीम पर बदलें" else "डार्क थीम पर बदलें"
            }
            .clip(CircleShape)
            .clickable {
                com.sih2026.palash.ui.theme.ThemeManager.toggleTheme(context)
            },
        shape = CircleShape,
        color = if (isDark) Color(0xFF322E36) else Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF625B6B) else Color(0xFFFFCC80)),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isDark) "🌙" else "☀️",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isDark) "डार्क" else "लाइट",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF0EAE1) else Color(0xFFD84315)
                )
            }
        }
    }
}

/**
 * Reusable M6 Top-Left Back Button.
 */
@Composable
fun M6BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.sih2026.palash.ui.theme.ThemeManager.isDarkMode.value

    Surface(
        modifier = modifier
            .semantics {
                testTag = "m6_back_button"
                contentDescription = "वापस"
            }
            .clip(CircleShape)
            .clickable { onBack() },
        shape = CircleShape,
        color = if (isDark) Color(0xFF322E36) else Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF625B6B) else Color(0xFFFFCC80)),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF0EAE1) else Color(0xFFD84315)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "वापस",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF0EAE1) else Color(0xFFD84315)
                )
            }
        }
    }
}

/**
 * Unified Top-Left Controls Row matching M6 specification:
 * Order/Priority: [Back]   [Dark Mode]
 */
@Composable
fun M6TopLeftRow(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        if (onBack != null) {
            M6BackButton(onBack = onBack)
            Spacer(modifier = Modifier.width(8.dp))
        }
        ThemeToggleButton()
    }
}



