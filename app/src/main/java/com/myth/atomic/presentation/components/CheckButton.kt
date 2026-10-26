package com.myth.atomic.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.myth.atomic.presentation.theme.AtomicTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * A round check that fills, draws its tick and throws a small burst when completed.
 * The touch target is 48dp; the visible circle is 26dp.
 */
@Composable
fun CheckButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AtomicTheme.colors.accent,
    checkColor: Color = AtomicTheme.colors.onAccent,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press",
    )
    val fill by animateFloatAsState(if (checked) 1f else 0f, tween(180), label = "fill")
    val tick by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(if (checked) 260 else 120, delayMillis = if (checked) 90 else 0, easing = FastOutSlowInEasing),
        label = "tick",
    )
    // Plays once per completion, not on first composition of an already-done task.
    val burst = remember { Animatable(1f) }
    var wasChecked by remember { mutableStateOf(checked) }
    LaunchedEffect(checked) {
        if (checked && !wasChecked) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        }
        wasChecked = checked
    }
    val outline = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .size(48.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox,
                interactionSource = interaction,
                indication = ripple(bounded = false, radius = 20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(26.dp).scale(scale)) {
            val radius = size.minDimension / 2
            val stroke = 2.dp.toPx()

            if (burst.value < 1f) {
                val progress = burst.value
                repeat(8) { i ->
                    val angle = Math.toRadians(i * 45.0 - 90)
                    val distance = radius * (1.15f + 0.75f * progress)
                    drawCircle(
                        color = color.copy(alpha = 1f - progress),
                        radius = 2.2.dp.toPx() * (1f - progress * 0.5f),
                        center = center + Offset((cos(angle) * distance).toFloat(), (sin(angle) * distance).toFloat()),
                    )
                }
            }

            drawCircle(outline.copy(alpha = 1f - fill), radius - stroke / 2, style = Stroke(stroke))
            if (fill > 0f) drawCircle(color, radius * fill)

            if (tick > 0f) {
                val path = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.67f)
                    lineTo(size.width * 0.73f, size.height * 0.36f)
                }
                val measure = PathMeasure().apply { setPath(path, false) }
                val partial = Path()
                measure.getSegment(0f, measure.length * tick, partial, true)
                drawPath(partial, checkColor, style = Stroke(2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}
