package com.devam.reminderapp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.devam.reminderapp.util.NotificationHelper

/**
 * Draws a small floating card on top of whatever app the user is currently in,
 * similar to Google's reminder pop-up. Tapping "Dismiss" removes it; "Snooze"
 * closes it (hook up re-scheduling logic there if you want real snooze behavior).
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, buildForegroundNotification())

        val title = intent?.getStringExtra("reminder_title") ?: "Reminder"
        val note = intent?.getStringExtra("reminder_note") ?: ""

        showOverlay(title, note)
        return START_NOT_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        NotificationHelper.createChannel(this)
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showOverlay(title: String, note: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F2F2F2"))
            setPadding(48, 40, 48, 40)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.BLACK)
        }

        val noteView = TextView(this).apply {
            text = note
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 12, 0, 24)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        val snoozeButton = Button(this).apply {
            text = "Snooze"
            setOnClickListener { removeOverlay() }
        }

        val dismissButton = Button(this).apply {
            text = "Dismiss"
            setOnClickListener { removeOverlay() }
        }

        buttonRow.addView(snoozeButton)
        buttonRow.addView(dismissButton)

        container.addView(titleView)
        if (note.isNotBlank()) container.addView(noteView)
        container.addView(buttonRow)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 80
        }

        overlayView = container
        windowManager?.addView(container, params)
    }

    private fun removeOverlay() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        stopSelf()
    }

    override fun onDestroy() {
        overlayView?.let { runCatching { windowManager?.removeView(it) } }
        super.onDestroy()
    }
}
