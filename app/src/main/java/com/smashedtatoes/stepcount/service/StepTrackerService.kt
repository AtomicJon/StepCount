package com.smashedtatoes.stepcount.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smashedtatoes.stepcount.MainActivity
import com.smashedtatoes.stepcount.R
import com.smashedtatoes.stepcount.data.db.StepDatabase
import com.smashedtatoes.stepcount.data.db.StepRecord
import com.smashedtatoes.stepcount.data.pref.UserPreferencesRepository
import com.smashedtatoes.stepcount.data.repository.StepRepositoryImpl
import com.smashedtatoes.stepcount.domain.StepCalculator
import com.smashedtatoes.stepcount.widget.StepCountWidgetProvider
import com.smashedtatoes.stepcount.widget.StepCountWidgetProvider1x1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StepTrackerService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var repository: StepRepositoryImpl

    companion object {
        const val CHANNEL_ID = "step_tracker_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TRACKING = "com.smashedtatoes.stepcount.ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.smashedtatoes.stepcount.ACTION_STOP_TRACKING"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val db = StepDatabase.getInstance(applicationContext)
        val prefs = UserPreferencesRepository(applicationContext)
        repository = StepRepositoryImpl(db.stepDao(), prefs)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_TRACKING
        if (action == ACTION_STOP_TRACKING) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, foregroundServiceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        registerStepSensor()
        return START_STICKY
    }

    private fun registerStepSensor() {
        stepSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val rawSensorValue = event.values[0].toInt()

        serviceScope.launch {
            processStepReading(rawSensorValue)
        }
    }

    private suspend fun processStepReading(rawSensorValue: Int) {
        val todayStr = StepCalculator.getCurrentDateString()
        val activeDate = repository.activeTrackingDateFlow.first()
        var baseline = repository.baselineSensorReadingFlow.first()
        var storedBase = repository.storedStepsBaseFlow.first()
        val dailyGoal = repository.dailyGoalFlow.first()

        if (activeDate.isEmpty() || activeDate != todayStr) {
            // Midnight date rollover or first launch on new day!
            baseline = rawSensorValue
            storedBase = 0
            repository.updateTrackingState(todayStr, baseline, storedBase)
        } else if (baseline < 0) {
            baseline = rawSensorValue
            repository.updateTrackingState(todayStr, baseline, storedBase)
        }

        val stepsToday = StepCalculator.calculateStepsForToday(rawSensorValue, baseline, storedBase)
        val todayRecord = StepRecord(date = todayStr, steps = stepsToday, goal = dailyGoal)
        repository.saveStepRecord(todayRecord)

        updateNotification(stepsToday, dailyGoal)
        StepCountWidgetProvider.updateAllWidgets(applicationContext)
        StepCountWidgetProvider1x1.updateAllWidgets(applicationContext)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracker Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your daily step count in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(steps: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedSteps = NumberFormat.getNumberInstance(Locale.US).format(steps)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Count")
            .setContentText("$formattedSteps steps today")
            .setSmallIcon(R.drawable.ic_home)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(steps: Int, goal: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        val formattedSteps = NumberFormat.getNumberInstance(Locale.US).format(steps)
        val formattedGoal = NumberFormat.getNumberInstance(Locale.US).format(goal)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$formattedSteps / $formattedGoal steps")
            .setContentText("Keep stepping! Step tracking active in background.")
            .setSmallIcon(R.drawable.ic_home)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
