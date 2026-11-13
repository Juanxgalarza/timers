package com.pixeltimers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Alarms don't survive a reboot or an app update, so re-arm every enabled reminder. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val scheduler = ReminderScheduler(context)
        ReminderStore(context).load().filter { it.enabled }.forEach { scheduler.start(it) }
    }
}
