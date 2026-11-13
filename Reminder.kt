package com.pixeltimers

enum class AlertMode { NOTIFY, VIBRATE, BOTH }

enum class ValidationError {
    EMPTY_LABEL, LABEL_TOO_LONG, INVALID_PATTERN, EMPTY_PATTERN, PATTERN_TOO_LONG, INTERVAL_OUT_OF_RANGE
}

data class Reminder(
    val id: Int,
    val label: String,
    // Minutes to wait before each alert; the list repeats as a cycle (e.g. 45, 5, 45, 10)
    val pattern: List<Int>,
    val mode: AlertMode,
    val enabled: Boolean = true,
) {
    init {
        require(pattern.isNotEmpty()) { "pattern must have at least one step" }
    }

    val notifies: Boolean get() = mode != AlertMode.VIBRATE
    val vibrates: Boolean get() = mode != AlertMode.NOTIFY

    // Modulo keeps stale step indexes safe after the pattern is edited
    fun intervalMillis(step: Int): Long = pattern[step % pattern.size] * 60_000L
    fun nextStep(step: Int): Int = (step + 1) % pattern.size

    companion object {
        const val MIN_INTERVAL_MINUTES = 1
        const val MAX_INTERVAL_MINUTES = 24 * 60
        const val MAX_PATTERN_STEPS = 12
        const val MAX_LABEL_LENGTH = 30

        fun validate(label: String, pattern: List<Int>): ValidationError? = when {
            label.isBlank() -> ValidationError.EMPTY_LABEL
            label.trim().length > MAX_LABEL_LENGTH -> ValidationError.LABEL_TOO_LONG
            pattern.isEmpty() -> ValidationError.EMPTY_PATTERN
            pattern.size > MAX_PATTERN_STEPS -> ValidationError.PATTERN_TOO_LONG
            pattern.any { it !in MIN_INTERVAL_MINUTES..MAX_INTERVAL_MINUTES } -> ValidationError.INTERVAL_OUT_OF_RANGE
            else -> null
        }

        // Ids double as PendingIntent request codes, so they must be unique among live reminders
        fun nextId(existing: List<Reminder>): Int = (existing.maxOfOrNull { it.id } ?: 0) + 1
    }
}

/** "45, 5 45;10" -> [45, 5, 45, 10]. Returns null if any token is not a number. */
fun parsePattern(raw: String): List<Int>? {
    val tokens = raw.split(Regex("[,;\\s]+")).filter { it.isNotEmpty() }
    return tokens.map { it.toIntOrNull() ?: return null }
}

fun formatInterval(minutes: Int): String = when {
    minutes % 60 == 0 -> "${minutes / 60} h"
    minutes > 60 -> "${minutes / 60} h ${minutes % 60} min"
    else -> "$minutes min"
}

fun formatPattern(pattern: List<Int>): String = pattern.joinToString(" → ") { formatInterval(it) }
