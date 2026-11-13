package com.pixeltimers

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ReminderTest {
    private val pattern = listOf(45, 5, 45, 10)

    @Test fun `valid input passes validation`() {
        assertNull(Reminder.validate("Estirar", pattern))
    }

    @Test fun `blank label is rejected`() {
        assertEquals(ValidationError.EMPTY_LABEL, Reminder.validate("   ", pattern))
    }

    @Test fun `overlong label is rejected`() {
        val label = "x".repeat(Reminder.MAX_LABEL_LENGTH + 1)
        assertEquals(ValidationError.LABEL_TOO_LONG, Reminder.validate(label, pattern))
    }

    @Test fun `empty pattern is rejected`() {
        assertEquals(ValidationError.EMPTY_PATTERN, Reminder.validate("a", emptyList()))
    }

    @Test fun `too many steps are rejected`() {
        val steps = List(Reminder.MAX_PATTERN_STEPS + 1) { 5 }
        assertEquals(ValidationError.PATTERN_TOO_LONG, Reminder.validate("a", steps))
    }

    @Test fun `interval bounds are inclusive`() {
        assertNull(Reminder.validate("a", listOf(Reminder.MIN_INTERVAL_MINUTES, Reminder.MAX_INTERVAL_MINUTES)))
        assertEquals(ValidationError.INTERVAL_OUT_OF_RANGE, Reminder.validate("a", listOf(5, 0)))
        assertEquals(
            ValidationError.INTERVAL_OUT_OF_RANGE,
            Reminder.validate("a", listOf(Reminder.MAX_INTERVAL_MINUTES + 1)),
        )
    }

    @Test fun `pattern cycles and wraps`() {
        val r = Reminder(1, "a", pattern, AlertMode.BOTH)
        assertEquals(1, r.nextStep(0))
        assertEquals(0, r.nextStep(3))
        assertEquals(5 * 60_000L, r.intervalMillis(1))
    }

    @Test fun `stale step index after editing the pattern stays in range`() {
        val r = Reminder(1, "a", listOf(30), AlertMode.BOTH)
        assertEquals(0, r.nextStep(3))
        assertEquals(30 * 60_000L, r.intervalMillis(3))
    }

    @Test fun `empty pattern cannot be constructed`() {
        assertThrows(IllegalArgumentException::class.java) { Reminder(1, "a", emptyList(), AlertMode.BOTH) }
    }

    @Test fun `parsePattern accepts commas spaces and semicolons`() {
        assertEquals(listOf(45, 5, 45, 10), parsePattern("45, 5, 45, 10"))
        assertEquals(listOf(45, 5, 45, 10), parsePattern("45 5;45,,10 "))
        assertEquals(listOf(30), parsePattern("30"))
    }

    @Test fun `parsePattern blank gives empty list and garbage gives null`() {
        assertEquals(emptyList<Int>(), parsePattern("  "))
        assertNull(parsePattern("45, x"))
        assertNull(parsePattern("99999999999"))
    }

    @Test fun `nextId starts at one and never collides`() {
        assertEquals(1, Reminder.nextId(emptyList()))
        val list = listOf(Reminder(1, "a", listOf(5), AlertMode.BOTH), Reminder(7, "b", listOf(5), AlertMode.BOTH))
        assertEquals(8, Reminder.nextId(list))
    }

    @Test fun `mode flags`() {
        assertEquals(true to false, Reminder(1, "a", listOf(5), AlertMode.NOTIFY).let { it.notifies to it.vibrates })
        assertEquals(false to true, Reminder(1, "a", listOf(5), AlertMode.VIBRATE).let { it.notifies to it.vibrates })
        assertEquals(true to true, Reminder(1, "a", listOf(5), AlertMode.BOTH).let { it.notifies to it.vibrates })
    }

    @Test fun `formatting`() {
        assertEquals("45 min", formatInterval(45))
        assertEquals("1 h", formatInterval(60))
        assertEquals("1 h 30 min", formatInterval(90))
        assertEquals("45 min → 5 min", formatPattern(listOf(45, 5)))
    }
}

class ReminderCodecTest {

    @Test fun `round trip preserves data`() {
        val original = listOf(
            Reminder(1, "Estirar", listOf(45, 5, 45, 10), AlertMode.BOTH, enabled = true),
            Reminder(2, "Agua", listOf(30), AlertMode.VIBRATE, enabled = false),
        )
        assertEquals(original, ReminderCodec.decode(ReminderCodec.encode(original)))
    }

    @Test fun `empty list round trips`() {
        assertEquals(emptyList<Reminder>(), ReminderCodec.decode(ReminderCodec.encode(emptyList())))
    }

    @Test fun `malformed json throws`() {
        assertThrows(JSONException::class.java) { ReminderCodec.decode("not json") }
    }

    @Test fun `unknown mode throws`() {
        val raw = """[{"id":1,"label":"a","pattern":[5],"mode":"LOUD","enabled":true}]"""
        assertThrows(JSONException::class.java) { ReminderCodec.decode(raw) }
    }

    @Test fun `empty pattern in storage throws json exception`() {
        val raw = """[{"id":1,"label":"a","pattern":[],"mode":"BOTH","enabled":true}]"""
        assertThrows(JSONException::class.java) { ReminderCodec.decode(raw) }
    }
}
