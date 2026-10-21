package com.sih2026.palash.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sih2026.palash.R

/**
 * M6 PALASH Logo Emblem.
 * Displays the official PALASH logo (APP_V2.png).
 */
@Composable
fun M6PalashLogo(
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "पलाश",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2f))
    )
}

/**
 * Recreated Top-Left Floral & Leaf Accent.
 * Renders the hanging branch with leaves and Palash blossoms on the top-left corner.
 */
@Composable
fun M6TopFloralAccent(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .semantics { contentDescription = "Top Left Floral Accent" }
    ) {
        val width = this.size.width
        val height = this.size.height

        // Branch stem path (curving from top-left)
        val branchPath = Path().apply {
            moveTo(0f, 0f)
            cubicTo(width * 0.15f, height * 0.3f, width * 0.35f, height * 0.2f, width * 0.45f, height * 0.05f)
        }
        drawPath(
            path = branchPath,
            color = Color(0xFF4E342E),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Green Leaves along branch
        val leafColors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50), Color(0xFF1B5E20))
        val leafPositions = listOf(
            Offset(width * 0.08f, height * 0.2f),
            Offset(width * 0.18f, height * 0.28f),
            Offset(width * 0.28f, height * 0.22f),
            Offset(width * 0.38f, height * 0.12f)
        )

        leafPositions.forEachIndexed { idx, pos ->
            drawCircle(
                color = leafColors[idx % leafColors.size],
                radius = 16.dp.toPx(),
                center = pos
            )
            // Leaf center vein
            drawLine(
                color = Color(0xFF81C784),
                start = Offset(pos.x - 8.dp.toPx(), pos.y - 8.dp.toPx()),
                end = Offset(pos.x + 8.dp.toPx(), pos.y + 8.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Palash Red/Orange Blossoms
        val flowerPositions = listOf(
            Offset(width * 0.05f, height * 0.35f),
            Offset(width * 0.14f, height * 0.45f),
            Offset(width * 0.25f, height * 0.35f)
        )
        flowerPositions.forEach { pos ->
            drawCircle(color = Color(0xFFD84315), radius = 14.dp.toPx(), center = pos)
            drawCircle(color = Color(0xFFFF5722), radius = 9.dp.toPx(), center = pos)
            drawCircle(color = Color(0xFFFFC107), radius = 4.dp.toPx(), center = pos)
        }
    }
}

/**
 * Recreated Bottom Village Landscape Component.
 * Draws rolling green hills, thatched tribal huts, winding path, and rising sun.
 */
@Composable
fun M6VillageLandscape(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { contentDescription = "Bottom Village Landscape Illustration" }
    ) {
        val w = this.size.width
        val h = this.size.height

        // Sun disc on horizon
        drawCircle(
            color = Color(0xFFFFE082),
            radius = 45.dp.toPx(),
            center = Offset(w * 0.65f, h * 0.45f)
        )

        // Background Hills (Far horizon)
        val hillFarPath = Path().apply {
            moveTo(0f, h * 0.6f)
            quadraticTo(w * 0.3f, h * 0.25f, w * 0.6f, h * 0.5f)
            quadraticTo(w * 0.85f, h * 0.35f, w, h * 0.55f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = hillFarPath, color = Color(0xFF81C784))

        // Foreground Hills (Near terrain)
        val hillNearPath = Path().apply {
            moveTo(0f, h * 0.72f)
            quadraticTo(w * 0.25f, h * 0.48f, w * 0.55f, h * 0.65f)
            quadraticTo(w * 0.8f, h * 0.5f, w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = hillNearPath, color = Color(0xFF4CAF50))

        // Winding dirt pathway
        val pathDirt = Path().apply {
            moveTo(w * 0.45f, h * 0.62f)
            quadraticTo(w * 0.52f, h * 0.75f, w * 0.4f, h * 0.9f)
            lineTo(w * 0.2f, h)
            lineTo(w * 0.6f, h)
            quadraticTo(w * 0.58f, h * 0.78f, w * 0.5f, h * 0.62f)
            close()
        }
        drawPath(path = pathDirt, color = Color(0xFFD7CCC8))

        // Thatched Tribal Huts (right side)
        val hutX1 = w * 0.68f
        val hutY1 = h * 0.52f
        // Walls
        drawRoundRect(
            color = Color(0xFFFFF3E0),
            topLeft = Offset(hutX1, hutY1 + 18.dp.toPx()),
            size = Size(46.dp.toPx(), 30.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        // Thatched roof (Triangle)
        val roof1 = Path().apply {
            moveTo(hutX1 - 6.dp.toPx(), hutY1 + 18.dp.toPx())
            lineTo(hutX1 + 23.dp.toPx(), hutY1 - 2.dp.toPx())
            lineTo(hutX1 + 52.dp.toPx(), hutY1 + 18.dp.toPx())
            close()
        }
        drawPath(path = roof1, color = Color(0xFF6D4C41))

        // Second Hut
        val hutX2 = w * 0.82f
        val hutY2 = h * 0.48f
        drawRoundRect(
            color = Color(0xFFFFF3E0),
            topLeft = Offset(hutX2, hutY2 + 20.dp.toPx()),
            size = Size(54.dp.toPx(), 34.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        val roof2 = Path().apply {
            moveTo(hutX2 - 6.dp.toPx(), hutY2 + 20.dp.toPx())
            lineTo(hutX2 + 27.dp.toPx(), hutY2 - 4.dp.toPx())
            lineTo(hutX2 + 60.dp.toPx(), hutY2 + 20.dp.toPx())
            close()
        }
        drawPath(path = roof2, color = Color(0xFF8D6E63))
    }
}

/**
 * Recreated Geometric Tribal / Santal Border Strip across screen bottom edge.
 */
@Composable
fun M6TribalBorder(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .semantics { contentDescription = "Tribal Santal Border Strip" }
    ) {
        val w = this.size.width
        val h = this.size.height

        // Border background band
        drawRect(color = Color(0xFFD84315))

        // Repeating white & dark diamond patterns
        val step = 20.dp.toPx()
        val count = (w / step).toInt() + 1

        for (i in 0..count) {
            val cx = i * step
            val diamondPath = Path().apply {
                moveTo(cx, 2.dp.toPx())
                lineTo(cx + step / 2f, h / 2f)
                lineTo(cx, h - 2.dp.toPx())
                lineTo(cx - step / 2f, h / 2f)
                close()
            }
            drawPath(path = diamondPath, color = Color(0xFFFFF3E0))
            drawCircle(color = Color(0xFF5D4037), radius = 2.dp.toPx(), center = Offset(cx, h / 2f))
        }
    }
}

/**
 * Recreated Student Waving Illustration Component matching M6 reference.
 */
@Composable
fun M6StudentIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Student Waving Illustration" }
    ) {
        val w = this.size.width
        val h = this.size.height

        // Student Shirt / Torso (White school shirt)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.25f, h * 0.52f),
            size = Size(w * 0.5f, h * 0.45f),
            cornerRadius = CornerRadius(12.dp.toPx())
        )

        // Red Backpack
        drawRoundRect(
            color = Color(0xFFD84315),
            topLeft = Offset(w * 0.12f, h * 0.5f),
            size = Size(w * 0.22f, h * 0.42f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Green Book in Hand
        drawRoundRect(
            color = Color(0xFF2E7D32),
            topLeft = Offset(w * 0.28f, h * 0.72f),
            size = Size(w * 0.28f, h * 0.24f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        // Head (Skin tone `#FFCC80`)
        drawCircle(
            color = Color(0xFFFFCC80),
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.32f)
        )

        // Hair (Dark brown `#3E2723`)
        val hairPath = Path().apply {
            addArc(
                oval = Rect(
                    left = w * 0.26f,
                    top = h * 0.08f,
                    right = w * 0.74f,
                    bottom = h * 0.36f
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f
            )
        }
        drawPath(path = hairPath, color = Color(0xFF3E2723))

        // Smiling Eyes & Mouth
        drawCircle(color = Color(0xFF3E2723), radius = 3.dp.toPx(), center = Offset(w * 0.42f, h * 0.32f))
        drawCircle(color = Color(0xFF3E2723), radius = 3.dp.toPx(), center = Offset(w * 0.58f, h * 0.32f))

        // Smile arc
        val smilePath = Path().apply {
            addArc(
                oval = Rect(
                    left = w * 0.43f,
                    top = h * 0.36f,
                    right = w * 0.57f,
                    bottom = h * 0.42f
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f
            )
        }
        drawPath(path = smilePath, color = Color(0xFFD84315), style = Stroke(width = 2.dp.toPx()))

        // Waving Hand (Right arm extended)
        val armPath = Path().apply {
            moveTo(w * 0.72f, h * 0.56f)
            lineTo(w * 0.88f, h * 0.42f)
        }
        drawPath(
            path = armPath,
            color = Color(0xFFFFCC80),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
        // Hand palm
        drawCircle(
            color = Color(0xFFFFCC80),
            radius = 7.dp.toPx(),
            center = Offset(w * 0.88f, h * 0.4f)
        )
    }
}

/**
 * Centralized Master M6 Background Artwork Component.
 * Wraps screens with warm background base, top floral accent, bottom village landscape, and tribal border.
 */
@Composable
fun M6BackgroundArtwork(
    modifier: Modifier = Modifier,
    showThemeToggle: Boolean = true,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = com.sih2026.palash.ui.theme.ThemeManager.isDarkMode.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "M6 Background Artwork Container" }
    ) {
        // Base Surface (Warm Cream in Light, Dark Charcoal in Dark Mode)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = if (isDark) Color(0xFF161418) else Color(0xFFFFF8F0))
        }

        // Top Left Floral Branch Accent
        M6TopFloralAccent(modifier = Modifier.align(Alignment.TopStart))

        // Bottom Village Landscape
        M6VillageLandscape(modifier = Modifier.align(Alignment.BottomCenter))

        // Dark mode overlay: subtle translucent tint over background artwork for crisp text contrast
        if (isDark) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = Color.Black.copy(alpha = 0.55f))
            }
        }

        // Screen Foreground Content
        content()

        // Bottom Geometric Tribal Border
        M6TribalBorder(modifier = Modifier.align(Alignment.BottomCenter))

        // TOP-LEFT Global Controls (Order: [Back]  [Dark Mode])
        if (showThemeToggle) {
            M6TopLeftRow(
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 12.dp, start = 12.dp)
            )
        }
    }
}
