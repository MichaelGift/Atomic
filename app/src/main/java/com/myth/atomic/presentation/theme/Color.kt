package com.myth.atomic.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Brand: ink and paper neutrals, one electric accent, and an ember for streaks and urgency.
private val Ink = Color(0xFF0D0E10)
private val InkRaised = Color(0xFF17191D)
private val Paper = Color(0xFFF5F4EF)
private val Lime = Color(0xFFD4FF3F)
private val EmberDark = Color(0xFFFF7A59)
private val EmberLight = Color(0xFFE2552F)

internal val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Ink,
    primaryContainer = Lime,
    onPrimaryContainer = Ink,
    secondary = EmberDark,
    onSecondary = Color(0xFF1A0A05),
    secondaryContainer = Color(0xFF3A1C14),
    onSecondaryContainer = Color(0xFFFFC3B3),
    tertiary = Color(0xFF7DB8FF),
    onTertiary = Ink,
    background = Ink,
    onBackground = Color(0xFFF2F2EE),
    surface = Ink,
    onSurface = Color(0xFFF2F2EE),
    surfaceVariant = Color(0xFF1C1F24),
    onSurfaceVariant = Color(0xFF9CA1AA),
    surfaceContainerLowest = Color(0xFF0A0B0D),
    surfaceContainerLow = Color(0xFF131518),
    surfaceContainer = InkRaised,
    surfaceContainerHigh = Color(0xFF1D2025),
    surfaceContainerHighest = Color(0xFF24272D),
    surfaceBright = Color(0xFF2A2D33),
    surfaceDim = Ink,
    inverseSurface = Color(0xFFF2F2EE),
    inverseOnSurface = Color(0xFF121316),
    inversePrimary = Color(0xFF5B7300),
    outline = Color(0xFF3A3E46),
    outlineVariant = Color(0xFF262930),
    error = Color(0xFFFF6B5A),
    onError = Color(0xFF1A0505),
    scrim = Color.Black,
)

internal val LightColors = lightColorScheme(
    primary = Color(0xFF141518),
    onPrimary = Paper,
    primaryContainer = Lime,
    onPrimaryContainer = Color(0xFF141518),
    secondary = EmberLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1D8),
    onSecondaryContainer = Color(0xFF3A1206),
    tertiary = Color(0xFF2F6FD0),
    onTertiary = Color.White,
    background = Paper,
    onBackground = Color(0xFF141518),
    surface = Paper,
    onSurface = Color(0xFF141518),
    surfaceVariant = Color(0xFFE9E8E1),
    onSurfaceVariant = Color(0xFF61656D),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFAF6),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEFEEE8),
    surfaceContainerHighest = Color(0xFFE6E5DE),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE6E5DE),
    inverseSurface = Color(0xFF1B1C1F),
    inverseOnSurface = Color(0xFFF2F2EE),
    inversePrimary = Lime,
    outline = Color(0xFFCFCDC5),
    outlineVariant = Color(0xFFE4E2DB),
    error = Color(0xFFD93A2B),
    onError = Color.White,
    scrim = Color.Black,
)

/** Colors the Material scheme has no slot for. Read through [AtomicTheme.colors]. */
@Immutable
data class AtomicColors(
    /** The brand accent: progress, checks, highlights. Always paired with [onAccent]. */
    val accent: Color,
    val onAccent: Color,
    /** Streaks and anything overdue. */
    val ember: Color,
    /** The unfilled part of rings and empty heatmap cells. */
    val track: Color,
    /** Heatmap intensity, from one completion to many. */
    val heat: List<Color>,
    val isDark: Boolean,
)

internal val DarkAtomicColors = AtomicColors(
    accent = Lime,
    onAccent = Ink,
    ember = EmberDark,
    track = Color(0xFF23262C),
    heat = listOf(0.28f, 0.5f, 0.75f, 1f).map { Lime.copy(alpha = it) },
    isDark = true,
)

internal val LightAtomicColors = AtomicColors(
    accent = Lime,
    onAccent = Color(0xFF141518),
    ember = EmberLight,
    track = Color(0xFFE6E5DE),
    // Lime alone washes out on paper, so the scale deepens towards olive.
    heat = listOf(Color(0xFFE8FF9C), Color(0xFFD4FF3F), Color(0xFF9EC21A), Color(0xFF5B7300)),
    isDark = false,
)

/** The colors a user can give a goal. Stored by index, so only ever append to this list. */
val GoalPalette = listOf(
    Color(0xFFC6F135), // lime
    Color(0xFF5AB0FF), // sky
    Color(0xFFFF7A59), // coral
    Color(0xFFA48BFF), // violet
    Color(0xFFFFC23D), // amber
    Color(0xFF3DDC97), // mint
)

fun goalColor(index: Int): Color = GoalPalette[index.mod(GoalPalette.size)]

/** Text that sits on a goal color; every palette entry is light enough for ink. */
val OnGoalColor = Color(0xFF141518)
