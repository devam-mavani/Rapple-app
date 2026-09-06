package com.devam.reminderapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

/**
 * Launches the system "display over other apps" settings screen for this app.
 * Call from MainActivity via: startActivity(Intent(this, OverlayPermissionActivity::class.java))
 */
class OverlayPermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
        finish()
    }
}
