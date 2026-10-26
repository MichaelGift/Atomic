package com.myth.atomic.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.myth.atomic.R

// Both families ship as single variable fonts; each weight is a point on the wght axis.
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: Int) = Font(
    resId = resId,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Display face: headings and numbers. */
val SpaceGrotesk = FontFamily(listOf(400, 500, 600, 700).map { variable(R.font.space_grotesk, it) })

/** Text face: everything people read in sentences. */
val Inter = FontFamily(listOf(400, 500, 600, 700).map { variable(R.font.inter, it) })

private fun display(size: Int, line: Int, weight: Int, tracking: Double = -0.02) = TextStyle(
    fontFamily = SpaceGrotesk,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em,
)

private fun text(size: Int, line: Int, weight: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em,
)

val AtomicTypography = Typography(
    displayLarge = display(64, 68, 600, -0.04),
    displayMedium = display(48, 52, 600, -0.035),
    displaySmall = display(36, 40, 600, -0.03),
    headlineLarge = display(32, 38, 600, -0.025),
    headlineMedium = display(28, 34, 600),
    headlineSmall = display(24, 30, 600),
    titleLarge = display(20, 26, 600, -0.01),
    titleMedium = text(16, 22, 600, -0.005),
    titleSmall = text(14, 20, 600),
    bodyLarge = text(16, 24, 400),
    bodyMedium = text(14, 20, 400),
    bodySmall = text(12, 16, 400),
    labelLarge = text(14, 20, 600),
    labelMedium = text(12, 16, 600, 0.01),
    labelSmall = text(11, 14, 600, 0.04),
)
