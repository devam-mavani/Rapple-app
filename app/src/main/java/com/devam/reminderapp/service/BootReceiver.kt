package com.devam.reminderapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import com.devam.reminderapp.data.ReminderDatabase
import com.devam.reminderapp.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val dao = ReminderDatabase.getInstance(context).reminderDao()
            CoroutineScope(Dispatchers.IO).launch {
                dao.getAll().collect { list ->
                    val now = System.currentTimeMillis()
                    list.filter { !it.isCompleted && it.triggerAtMillis > now }
                        .forEach { AlarmScheduler.schedule(context, it) }
                    return@collect // only need the first emission
                }
            }
        }
    }
}
