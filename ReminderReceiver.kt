package com.pixeltimers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, NO_ID)
        if (id == NO_ID) {
            Log.w(TAG, "Alarm fired without a reminder id")
            return
        }

        val reminder = ReminderStore(context).load().firstOrNull { it.id == id }
        if (reminder == null || !reminder.enabled) {
            Log.i(TAG, "Reminder $id deleted or disabled, dropping alarm")
            return
        }

        // The gap to the next alert is measured from this one, so lateness never shortens it
        val nextStep = reminder.nextStep(intent.getIntExtra(EXTRA_STEP, 0))
        val nextAt = System.currentTimeMillis() + reminder.intervalMillis(nextStep)

        // Re-arm first (alarms are one-shot) so a failure while alerting can't break the chain
        ReminderScheduler(context).schedule(reminder, nextStep, nextAt)
        Alerts.fire(context, reminder, nextInMinutes = reminder.pattern[nextStep])
    }

    companion object {
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_STEP = "pattern_step"
        private const val NO_ID = -1
        private const val TAG = "ReminderReceiver"
    }
}
