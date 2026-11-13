package com.pixeltimers

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Alerts {
    private const val TAG = "Alerts"
    private const val CHANNEL_ID = "quiet_reminders"

    // Two short taps: noticeable in a pocket, not annoying
    private val VIBRATION_PATTERN = longArrayOf(0, 90, 70, 90)

    fun fire(context: Context, reminder: Reminder, nextInMinutes: Int) {
        if (reminder.notifies) postNotification(context, reminder, nextInMinutes)
        if (reminder.vibrates) vibrate(context)
    }

    @SuppressLint("MissingPermission") // Guarded by the explicit POST_NOTIFICATIONS check below
    private fun postNotification(context: Context, reminder: Reminder, nextInMinutes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted, skipping notification")
            return
        }
        ensureChannel(context)

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(reminder.label)
            .setContentText(context.getString(R.string.notification_text, formatInterval(nextInMinutes)))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        // Reusing the reminder id means a new tick replaces the old one instead of stacking
        NotificationManagerCompat.from(context).notify(reminder.id, notification)
    }

    private fun ensureChannel(context: Context) {
        // IMPORTANCE_LOW: shows in the shade, no sound, no heads-up popup
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.channel_description)
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.i(TAG, "No vibrator available")
            return
        }
        // Usage matters: the system may drop background vibrations with an unknown usage
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1), attributes)
    }
}
