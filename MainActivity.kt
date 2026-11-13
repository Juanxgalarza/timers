package com.pixeltimers

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pixeltimers.ui.ReminderScreen

class MainActivity : ComponentActivity() {
    private val viewModel: ReminderViewModel by viewModels()

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) Log.i(TAG, "Notification permission denied; vibration-only alerts still work")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        requestNotificationPermissionIfNeeded()

        setContent {
            val reminders = viewModel.reminders.collectAsStateWithLifecycle().value
            val needsExactAccess = viewModel.needsExactAccess.collectAsStateWithLifecycle().value
            ReminderScreen(
                reminders = reminders,
                needsExactAccess = needsExactAccess,
                onSave = viewModel::save,
                onToggle = viewModel::setEnabled,
                onDelete = viewModel::delete,
                onGrantExactAccess = ::openExactAlarmSettings,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // The user may have just granted exact alarms from system settings
        viewModel.refreshExactAccess()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Exact alarm settings screen not available on this device", e)
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
