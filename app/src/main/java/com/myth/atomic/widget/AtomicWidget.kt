package com.myth.atomic.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.color.ColorProvider
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import com.myth.atomic.presentation.theme.DarkColors
import com.myth.atomic.presentation.theme.LightColors
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.myth.atomic.MainActivity
import com.myth.atomic.R
import dagger.hilt.android.EntryPointAccessors

/** Home-screen widget: today's open tasks, checkable in place, plus the current streak. */
class AtomicWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = EntryPointAccessors.fromApplication<WidgetEntryPoint>(context).widgetDataSource()
        val initial = dataSource.load()
        provideContent {
            // Live while the Glance session runs; later changes reach the widget through
            // AtomicApplication calling updateAll, which starts a new session.
            val state by dataSource.observe().collectAsState(initial)
            GlanceTheme(colors = WidgetColors) {
                WidgetContent(state)
            }
        }
    }

    /** The picture shown in the launcher's widget picker on Android 15+. */
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val sample = WidgetState(
            tasks = listOf(
                WidgetTask(1, context.getString(R.string.widget_preview_task_1), "", isOverdue = true),
                WidgetTask(2, context.getString(R.string.widget_preview_task_2), "", isOverdue = false),
                WidgetTask(3, context.getString(R.string.widget_preview_task_3), "", isOverdue = false),
            ),
            totalDue = 3,
            streak = 6,
            doneToday = 2,
        )
        provideContent {
            GlanceTheme(colors = WidgetColors) {
                WidgetContent(sample)
            }
        }
    }
}

class AtomicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AtomicWidget()
}

class CompleteTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdKey] ?: return
        EntryPointAccessors.fromApplication<WidgetEntryPoint>(context).taskRepository().setTaskCompleted(taskId, true)
        AtomicWidget().updateAll(context)
    }

    companion object {
        val TaskIdKey = ActionParameters.Key<Long>("taskId")
    }
}

private val WidgetColors = ColorProviders(light = LightColors, dark = DarkColors)
private val Accent = ColorProvider(day = Color(0xFFD4FF3F), night = Color(0xFFD4FF3F))
private val AccentText = ColorProvider(day = Color(0xFF141518), night = Color(0xFFD4FF3F))

@Composable
private fun WidgetContent(state: WidgetState) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(16.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_atom),
                contentDescription = null,
                colorFilter = ColorFilter.tint(AccentText),
                modifier = GlanceModifier.size(18.dp),
            )
            Text(
                context.getString(R.string.nav_today),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.defaultWeight().padding(start = 8.dp),
            )
            if (state.streak > 0) {
                Text(
                    context.resources.getQuantityString(R.plurals.widget_streak, state.streak, state.streak),
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GlanceTheme.colors.secondary),
                )
            }
        }
        Spacer(GlanceModifier.height(10.dp))
        LinearProgressIndicator(
            progress = state.dayProgress,
            color = Accent,
            backgroundColor = GlanceTheme.colors.surfaceVariant,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp).cornerRadius(3.dp),
        )
        Spacer(GlanceModifier.height(8.dp))
        if (state.tasks.isEmpty()) {
            Text(
                context.getString(R.string.widget_all_clear),
                style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
            )
        } else {
            LazyColumn {
                items(state.tasks, itemId = { it.id }) { task ->
                    CheckBox(
                        checked = false,
                        onCheckedChange = actionRunCallback<CompleteTaskAction>(
                            actionParametersOf(CompleteTaskAction.TaskIdKey to task.id),
                        ),
                        text = task.title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = if (task.isOverdue) GlanceTheme.colors.secondary else GlanceTheme.colors.onSurface,
                        ),
                        colors = CheckboxDefaults.colors(checkedColor = Accent, uncheckedColor = GlanceTheme.colors.onSurfaceVariant),
                        maxLines = 1,
                    )
                }
                if (state.totalDue > state.tasks.size) {
                    item {
                        Text(
                            (state.totalDue - state.tasks.size).let { context.resources.getQuantityString(R.plurals.widget_more, it, it) },
                            style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant),
                            modifier = GlanceModifier.padding(top = 4.dp).clickable(actionStartActivity<MainActivity>()),
                        )
                    }
                }
            }
        }
    }
}
