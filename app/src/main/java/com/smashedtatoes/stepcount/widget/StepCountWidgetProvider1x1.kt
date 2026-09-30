package com.smashedtatoes.stepcount.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.smashedtatoes.stepcount.MainActivity
import com.smashedtatoes.stepcount.R
import com.smashedtatoes.stepcount.data.db.StepDatabase
import com.smashedtatoes.stepcount.data.pref.UserPreferencesRepository
import com.smashedtatoes.stepcount.domain.StepCalculator
import com.smashedtatoes.stepcount.ui.components.formatStepCountShort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StepCountWidgetProvider1x1 : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java).let { intent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val views = RemoteViews(context.packageName, R.layout.widget_step_count_1x1).apply {
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val db = StepDatabase.getInstance(context)
                val prefs = UserPreferencesRepository(context)
                val todayStr = StepCalculator.getCurrentDateString()

                val todayRecord = db.stepDao().getRecordForDate(todayStr)
                val steps = todayRecord?.steps ?: 0
                val goal = prefs.dailyGoalFlow.first()

                val shortSteps = formatStepCountShort(steps)
                val percent = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt() else 0

                views.setTextViewText(R.id.widget_steps_text, shortSteps)
                views.setTextViewText(R.id.widget_percent_text, "$percent%")

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, StepCountWidgetProvider1x1::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
