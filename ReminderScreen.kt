package com.pixeltimers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixeltimers.AlertMode
import com.pixeltimers.R
import com.pixeltimers.Reminder
import com.pixeltimers.ValidationError
import com.pixeltimers.formatPattern

@Composable
fun ReminderScreen(
    reminders: List<Reminder>,
    needsExactAccess: Boolean,
    onSave: (editingId: Int?, label: String, patternText: String, mode: AlertMode) -> ValidationError?,
    onToggle: (id: Int, enabled: Boolean) -> Unit,
    onDelete: (id: Int) -> Unit,
    onGrantExactAccess: () -> Unit,
) {
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(PixelColors.Background)
            .systemBarsPadding()
            .padding(16.dp),
    ) {
        PixelText(stringResource(R.string.title_app), color = PixelColors.Accent, size = 24.sp, bold = true)
        Spacer(Modifier.height(12.dp))

        if (needsExactAccess) {
            PixelPanel(
                Modifier.fillMaxWidth().clickable(onClick = onGrantExactAccess),
                fill = PixelColors.PanelDark,
                border = PixelColors.Orange,
            ) {
                PixelText(stringResource(R.string.exact_banner), color = PixelColors.Orange, size = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        if (reminders.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                PixelText(stringResource(R.string.empty_state), color = PixelColors.Muted)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onEdit = { editing = reminder; editorOpen = true },
                        onToggle = { onToggle(reminder.id, it) },
                        onDelete = { onDelete(reminder.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        PixelButton(
            text = stringResource(R.string.add_timer),
            onClick = { editing = null; editorOpen = true },
            modifier = Modifier.fillMaxWidth(),
            fill = PixelColors.Accent,
            content = PixelColors.Background,
        )
    }

    if (editorOpen) {
        ReminderEditorDialog(
            initial = editing,
            onDismiss = { editorOpen = false },
            onSave = { label, patternText, mode -> onSave(editing?.id, label, patternText, mode) },
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val on = reminder.enabled
    PixelPanel(border = if (on) PixelColors.Accent else PixelColors.Muted) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f).clickable(onClick = onEdit),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PixelText(
                    reminder.label,
                    color = if (on) PixelColors.Text else PixelColors.Muted,
                    size = 16.sp,
                    bold = true,
                )
                PixelText(
                    stringResource(R.string.every_format, formatPattern(reminder.pattern), modeLabel(reminder.mode))
                        .uppercase(),
                    color = PixelColors.Muted,
                    size = 12.sp,
                )
            }
            Spacer(Modifier.width(8.dp))
            PixelButton(
                text = stringResource(if (on) R.string.toggle_on else R.string.toggle_off),
                onClick = { onToggle(!on) },
                fill = if (on) PixelColors.Green else PixelColors.PanelDark,
                content = if (on) PixelColors.Background else PixelColors.Text,
                description = stringResource(R.string.toggle_description),
            )
            Spacer(Modifier.width(8.dp))
            PixelButton(
                text = stringResource(R.string.delete_label),
                onClick = onDelete,
                fill = PixelColors.Red,
                description = stringResource(R.string.delete_description),
            )
        }
    }
}

@Composable
private fun ReminderEditorDialog(
    initial: Reminder?,
    onDismiss: () -> Unit,
    onSave: (label: String, patternText: String, mode: AlertMode) -> ValidationError?,
) {
    var label by remember { mutableStateOf(initial?.label.orEmpty()) }
    var patternText by remember { mutableStateOf(initial?.pattern?.joinToString(", ").orEmpty()) }
    var mode by remember { mutableStateOf(initial?.mode ?: AlertMode.NOTIFY) }
    var error by remember { mutableStateOf<ValidationError?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        PixelPanel(Modifier.fillMaxWidth(), fill = PixelColors.Background) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PixelText(
                    stringResource(if (initial == null) R.string.editor_new else R.string.editor_edit),
                    color = PixelColors.Accent,
                    size = 18.sp,
                    bold = true,
                )
                PixelField(
                    value = label,
                    onValueChange = { label = it.take(Reminder.MAX_LABEL_LENGTH) },
                    placeholder = stringResource(R.string.field_label_hint),
                )
                PixelField(
                    value = patternText,
                    onValueChange = { patternText = it.filter { c -> c.isDigit() || c == ',' || c == ' ' }.take(60) },
                    placeholder = stringResource(R.string.field_pattern_hint),
                    keyboardType = KeyboardType.Text,
                )
                PixelText(stringResource(R.string.pattern_help), color = PixelColors.Muted, size = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertMode.entries.forEach { option ->
                        val selected = option == mode
                        PixelButton(
                            text = modeLabel(option).uppercase(),
                            onClick = { mode = option },
                            modifier = Modifier.weight(1f),
                            fill = if (selected) PixelColors.Accent else PixelColors.PanelDark,
                            content = if (selected) PixelColors.Background else PixelColors.Text,
                        )
                    }
                }
                error?.let { PixelText(errorText(it), color = PixelColors.Orange, size = 12.sp) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelButton(stringResource(R.string.cancel), onDismiss, Modifier.weight(1f))
                    PixelButton(
                        text = stringResource(R.string.save),
                        onClick = {
                            val result = onSave(label, patternText, mode)
                            if (result == null) onDismiss() else error = result
                        },
                        modifier = Modifier.weight(1f),
                        fill = PixelColors.Green,
                        content = PixelColors.Background,
                    )
                }
            }
        }
    }
}

@Composable
private fun modeLabel(mode: AlertMode): String = stringResource(
    when (mode) {
        AlertMode.NOTIFY -> R.string.mode_notify
        AlertMode.VIBRATE -> R.string.mode_vibrate
        AlertMode.BOTH -> R.string.mode_both
    }
)

@Composable
private fun errorText(error: ValidationError): String = when (error) {
    ValidationError.EMPTY_LABEL -> stringResource(R.string.error_empty_label)
    ValidationError.LABEL_TOO_LONG -> stringResource(R.string.error_label_too_long, Reminder.MAX_LABEL_LENGTH)
    ValidationError.INVALID_PATTERN -> stringResource(R.string.error_invalid_pattern)
    ValidationError.EMPTY_PATTERN -> stringResource(R.string.error_empty_pattern)
    ValidationError.PATTERN_TOO_LONG -> stringResource(R.string.error_pattern_too_long, Reminder.MAX_PATTERN_STEPS)
    ValidationError.INTERVAL_OUT_OF_RANGE ->
        stringResource(R.string.error_interval, Reminder.MIN_INTERVAL_MINUTES, Reminder.MAX_INTERVAL_MINUTES)
}
