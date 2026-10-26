package com.myth.atomic.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.myth.atomic.R
import com.myth.atomic.domain.stats.DayCount
import com.myth.atomic.domain.stats.ProgressStats
import com.myth.atomic.presentation.theme.AtomicTheme
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
fun ProgressCard(stats: ProgressStats, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                StatBlock(stats.currentStreak.toString(), stringResource(R.string.stats_current_streak), Modifier.weight(1f))
                StatBlock(stats.longestStreak.toString(), stringResource(R.string.stats_best_streak), Modifier.weight(1f))
                StatBlock(stats.completedThisWeek.toString(), stringResource(R.string.stats_this_week), Modifier.weight(1f))
            }
            if (stats.currentStreak == 0) {
                Text(
                    stringResource(if (stats.hasHistory) R.string.stats_streak_restart else R.string.stats_streak_start),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WeekBars(stats.lastSevenDays)
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A seven-day bar chart of completed tasks; today is the rightmost bar. */
@Composable
private fun WeekBars(days: List<DayCount>) {
    val max = days.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val locale = LocalConfiguration.current.locales[0]
    val summary = days.joinToString { day ->
        "${day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)} ${day.count}"
    }
    val description = pluralStringResource(R.plurals.stats_week_description, days.sumOf { it.count }, days.sumOf { it.count }, summary)

    Row(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEachIndexed { index, day ->
            val isToday = index == days.lastIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.height(56.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .width(20.dp)
                            .height(if (day.count == 0) 4.dp else (56 * day.count / max).coerceAtLeast(8).dp)
                            .background(
                                if (day.count == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(6.dp),
                            ),
                    )
                }
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProgressCardPreview() {
    val today = LocalDate.of(2026, 3, 14)
    AtomicTheme(dynamicColor = false) {
        ProgressCard(
            ProgressStats(
                currentStreak = 4,
                longestStreak = 9,
                lastSevenDays = listOf(0, 2, 1, 3, 1, 2, 1).mapIndexed { i, n -> DayCount(today.minusDays(6L - i), n) },
            ),
        )
    }
}
