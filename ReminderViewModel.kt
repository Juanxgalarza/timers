package com.pixeltimers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ReminderStore(application)
    private val scheduler = ReminderScheduler(application)

    private val _reminders = MutableStateFlow(store.load())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _needsExactAccess = MutableStateFlow(!scheduler.canScheduleExact())
    val needsExactAccess: StateFlow<Boolean> = _needsExactAccess.asStateFlow()

    fun refreshExactAccess() {
        _needsExactAccess.value = !scheduler.canScheduleExact()
    }

    /** Creates a reminder, or updates it when [editingId] is set. Returns null on success. */
    fun save(editingId: Int?, label: String, patternText: String, mode: AlertMode): ValidationError? {
        val pattern = parsePattern(patternText) ?: return ValidationError.INVALID_PATTERN
        Reminder.validate(label, pattern)?.let { return it }

        val current = _reminders.value
        val existing = current.firstOrNull { it.id == editingId }
        val reminder = Reminder(
            id = existing?.id ?: Reminder.nextId(current),
            label = label.trim(),
            pattern = pattern,
            mode = mode,
            enabled = existing?.enabled ?: true,
        )
        commit(if (existing == null) current + reminder else current.map { if (it.id == reminder.id) reminder else it })
        applySchedule(reminder)
        return null
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        val current = _reminders.value
        val target = current.firstOrNull { it.id == id } ?: return
        val updated = target.copy(enabled = enabled)
        commit(current.map { if (it.id == id) updated else it })
        applySchedule(updated)
    }

    fun delete(id: Int) {
        scheduler.cancel(id)
        commit(_reminders.value.filterNot { it.id == id })
    }

    private fun applySchedule(reminder: Reminder) {
        if (reminder.enabled) scheduler.start(reminder) else scheduler.cancel(reminder.id)
    }

    private fun commit(list: List<Reminder>) {
        store.save(list)
        _reminders.value = list
    }
}
