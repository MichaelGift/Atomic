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
import androidx.glance.appwidget.CheckBox
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
            GlanceTheme {
                WidgetContent(state)
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

@Composable
private fun WidgetContent(state: WidgetState) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                context.getString(R.string.nav_today),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (state.streak > 0) {
                Text(
                    context.resources.getQuantityString(R.plurals.widget_streak, state.streak, state.streak),
                    style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.primary),
                )
            }
        }
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
                            color = if (task.isOverdue) GlanceTheme.colors.error else GlanceTheme.colors.onSurface,
                        ),
                        maxLines = 1,
                    )
                }
                if (state.totalDue > state.tasks.size) {
                    item {
                        Text(
                            context.getString(R.string.widget_more, state.totalDue - state.tasks.size),
                            style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant),
                            modifier = GlanceModifier.padding(top = 4.dp).clickable(actionStartActivity<MainActivity>()),
                        )
                    }
                }
            }
        }
    }
}
