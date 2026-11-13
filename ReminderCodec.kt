package com.pixeltimers

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object ReminderCodec {

    fun encode(reminders: List<Reminder>): String {
        val array = JSONArray()
        reminders.forEach { r ->
            array.put(
                JSONObject()
                    .put("id", r.id)
                    .put("label", r.label)
                    .put("pattern", JSONArray(r.pattern))
                    .put("mode", r.mode.name)
                    .put("enabled", r.enabled)
            )
        }
        return array.toString()
    }

    /** @throws JSONException if the payload is malformed, has an empty pattern or an unknown mode. */
    fun decode(raw: String): List<Reminder> {
        val array = JSONArray(raw)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val modeName = o.getString("mode")
            val steps = o.getJSONArray("pattern")
            val pattern = (0 until steps.length()).map { steps.getInt(it) }
            if (pattern.isEmpty()) throw JSONException("Empty pattern")
            Reminder(
                id = o.getInt("id"),
                label = o.getString("label"),
                pattern = pattern,
                mode = AlertMode.entries.firstOrNull { it.name == modeName }
                    ?: throw JSONException("Unknown mode: $modeName"),
                enabled = o.optBoolean("enabled", true),
            )
        }
    }
}
