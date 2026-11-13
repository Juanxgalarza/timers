package com.pixeltimers

import android.content.Context
import android.util.Log
import org.json.JSONException

class ReminderStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<Reminder> {
        val raw = prefs.getString(KEY_REMINDERS, null) ?: return emptyList()
        return try {
            ReminderCodec.decode(raw)
        } catch (e: JSONException) {
            // Called from receivers too: log and start clean instead of crashing the alarm chain
            Log.e(TAG, "Could not decode stored reminders, starting empty", e)
            emptyList()
        }
    }

    fun save(reminders: List<Reminder>) {
        prefs.edit().putString(KEY_REMINDERS, ReminderCodec.encode(reminders)).apply()
    }

    private companion object {
        const val TAG = "ReminderStore"
        const val PREFS_NAME = "reminders"
        const val KEY_REMINDERS = "list"
    }
}
