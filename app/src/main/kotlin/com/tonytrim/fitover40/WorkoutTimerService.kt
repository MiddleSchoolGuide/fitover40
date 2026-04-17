package com.tonytrim.fitover40

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.tonytrim.fitover40.ui.running.WorkoutPhase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class WorkoutTimerService : Service() {

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _secondsRemaining = MutableStateFlow(0)
    val secondsRemaining = _secondsRemaining.asStateFlow()

    private val _currentPhase = MutableStateFlow(WorkoutPhase.WARM_UP)
    val currentPhase = _currentPhase.asStateFlow()

    private var workoutLabel = "Workout"

    inner class TimerBinder : Binder() {
        fun getService(): WorkoutTimerService = this@WorkoutTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopForegroundService()
        }
        return START_STICKY
    }

    fun updateWorkoutState(phase: WorkoutPhase, seconds: Int, label: String) {
        _currentPhase.value = phase
        _secondsRemaining.value = seconds
        workoutLabel = label
        updateNotification()
    }

    private fun startForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                if (_secondsRemaining.value > 0) {
                    _secondsRemaining.value -= 1
                    updateNotification()
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        updateNotification(isPaused = true)
    }

    private fun stopForegroundService() {
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(isPaused: Boolean = false): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val phaseText = when (_currentPhase.value) {
            WorkoutPhase.WARM_UP -> "Warm Up"
            WorkoutPhase.RUN -> "Running"
            WorkoutPhase.WALK -> "Walking"
            WorkoutPhase.COOL_DOWN -> "Cool Down"
            WorkoutPhase.FINISHED -> "Finished"
        }

        val timeText = String.format(Locale.US, "%02d:%02d", _secondsRemaining.value / 60, _secondsRemaining.value % 60)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // Replace with app icon later
            .setContentTitle(workoutLabel)
            .setContentText("$phaseText • $timeText remaining")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)

        // Actions
        val pauseActionIntent = Intent(this, WorkoutTimerService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseActionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val stopActionIntent = Intent(this, WorkoutTimerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 2, stopActionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (isPaused) {
            val startActionIntent = Intent(this, WorkoutTimerService::class.java).apply { action = ACTION_START }
            val startPendingIntent = PendingIntent.getService(this, 3, startActionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "Resume", startPendingIntent)
        } else {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
        }
        
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)

        return builder.build()
    }

    private fun updateNotification(isPaused: Boolean = false) {
        val notification = createNotification(isPaused)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active workout progress"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "workout_timer_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
