package com.myth.atomic.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val AtomicShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private val LocalAtomicColors = staticCompositionLocalOf { DarkAtomicColors }

/**
 * Atomic's own palette and type rather than dynamic color: the brand is the product here.
 * Follows the system light/dark setting.
 */
@Composable
fun AtomicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAtomicColors provides if (darkTheme) DarkAtomicColors else LightAtomicColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AtomicTypography,
            shapes = AtomicShapes,
            content = content,
        )
    }
}

object AtomicTheme {
    val colors: AtomicColors
        @Composable @ReadOnlyComposable
        get() = LocalAtomicColors.current
}
