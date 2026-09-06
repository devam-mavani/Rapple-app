package com.devam.reminderapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.devam.reminderapp.util.NotificationHelper

/**
 * Fired by AlarmManager at the reminder's scheduled time.
 * Always posts a normal notification (works everywhere), and additionally
 * launches the floating overlay if the user has granted "display over other apps".
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("reminder_id", -1)
        val title = intent.getStringExtra("reminder_title") ?: "Reminder"
        val note = intent.getStringExtra("reminder_note") ?: ""

        NotificationHelper.show(context, id, title, note)

        val canOverlay = Settings.canDrawOverlays(context)
        if (canOverlay) {
            val overlayIntent = Intent(context, OverlayService::class.java).apply {
                putExtra("reminder_id", id)
                putExtra("reminder_title", title)
                putExtra("reminder_note", note)
            }
            context.startForegroundService(overlayIntent)
        }
    }
}
