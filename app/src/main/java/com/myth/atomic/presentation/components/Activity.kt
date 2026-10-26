package com.myth.atomic.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myth.atomic.R
import com.myth.atomic.domain.stats.DayCount
import com.myth.atomic.presentation.theme.AtomicTheme
import java.time.format.TextStyle

@Composable
private fun heatColor(count: Int): Color {
    val colors = AtomicTheme.colors
    return when {
        count <= 0 -> colors.track
        count >= colors.heat.size -> colors.heat.last()
        else -> colors.heat[count - 1]
    }
}

/**
 * A contribution-style grid: one column per week, one row per weekday, shaded by tasks done.
 * Expects [days] to start on the first day of a week, as ProgressStats provides them.
 */
@Composable
fun ActivityHeatmap(days: List<DayCount>, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val weeks = days.chunked(7)
    val total = days.sumOf { it.count }
    val activeDays = days.count { it.count > 0 }
    val description = pluralStringResource(R.plurals.heatmap_description, total, total, activeDays, weeks.size)
    val gap = 4.dp

    Column(modifier.clearAndSetSemantics { contentDescription = description }) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val labelWidth = 18.dp
            val cell = ((maxWidth - labelWidth - gap * weeks.size) / weeks.size).coerceAtMost(22.dp)
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                // Month labels above the first week of each month.
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    Spacer(Modifier.width(labelWidth - gap))
                    weeks.forEachIndexed { index, week ->
                        val first = week.first().date
                        val showMonth = index == 0 || weeks[index - 1].first().date.month != first.month
                        Box(Modifier.width(cell)) {
                            if (showMonth) {
                                Text(
                                    first.month.getDisplayName(TextStyle.SHORT, locale),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
                for (weekday in 0 until 7) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            // Label alternate rows, as calendars do, to keep the grid quiet.
                            if (weekday % 2 == 0) weeks.first()[weekday].date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale) else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(labelWidth - gap),
                        )
                        weeks.forEach { week ->
                            val day = week.getOrNull(weekday)
                            Box(
                                Modifier
                                    .size(cell)
                                    .background(
                                        if (day == null) Color.Transparent else heatColor(day.count),
                                        RoundedCornerShape(cell * 0.28f),
                                    ),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.heatmap_less),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            (0..AtomicTheme.colors.heat.size).forEach { level ->
                Box(Modifier.size(10.dp).background(heatColor(level), RoundedCornerShape(3.dp)))
            }
            Text(
                stringResource(R.string.heatmap_more),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The last seven days as letter-and-dot columns; today is outlined. */
@Composable
fun WeekStrip(days: List<DayCount>, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val accent = AtomicTheme.colors.accent
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { index, day ->
            val isToday = index == days.lastIndex
            val done = day.count > 0
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Box(
                    Modifier
                        .size(22.dp)
                        .background(if (done) accent else AtomicTheme.colors.track, CircleShape)
                        .then(if (isToday && !done) Modifier.border(1.5.dp, accent, CircleShape) else Modifier),
                )
            }
        }
    }
}
