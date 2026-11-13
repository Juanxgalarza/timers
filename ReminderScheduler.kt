package com.pixeltimers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReminderScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Arms the first step of the pattern, replacing any alarm already armed for the reminder. */
    fun start(reminder: Reminder, now: Long = System.currentTimeMillis()) =
        schedule(reminder, step = 0, triggerAtMillis = now + reminder.intervalMillis(0))

    /** [step] is the pattern position that ends when this alarm fires. */
    fun schedule(reminder: Reminder, step: Int, triggerAtMillis: Long) {
        val intent = intentFor(reminder.id).putExtra(ReminderReceiver.EXTRA_STEP, step)
        // Same request code => this replaces any alarm already armed for the reminder
        val pending = PendingIntent.getBroadcast(
            appContext, reminder.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
                return
            }
        } catch (e: SecurityException) {
            // Permission can be revoked between the check and the call
            Log.w(TAG, "Exact alarms not permitted, falling back to inexact", e)
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
    }

    fun cancel(id: Int) {
        val existing = PendingIntent.getBroadcast(
            appContext, id, intentFor(id),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(existing)
        existing.cancel()
    }

    private fun intentFor(id: Int) =
        Intent(appContext, ReminderReceiver::class.java).putExtra(ReminderReceiver.EXTRA_ID, id)

    private companion object {
        const val TAG = "ReminderScheduler"
    }
}
