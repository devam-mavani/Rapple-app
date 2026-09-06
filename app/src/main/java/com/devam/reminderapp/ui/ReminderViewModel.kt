package com.devam.reminderapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devam.reminderapp.data.Reminder
import com.devam.reminderapp.data.ReminderDatabase
import com.devam.reminderapp.data.ReminderRepository
import com.devam.reminderapp.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReminderRepository(
        ReminderDatabase.getInstance(application).reminderDao()
    )

    val reminders: StateFlow<List<Reminder>> = repository.allReminders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addReminder(title: String, note: String, triggerAtMillis: Long) {
        viewModelScope.launch {
            val id = repository.add(
                Reminder(title = title, note = note, triggerAtMillis = triggerAtMillis)
            )
            val saved = repository.getById(id) ?: return@launch
            AlarmScheduler.schedule(getApplication(), saved)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            AlarmScheduler.cancel(getApplication(), reminder)
            repository.delete(reminder)
        }
    }

    fun toggleCompleted(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder.copy(isCompleted = !reminder.isCompleted))
            if (!reminder.isCompleted) {
                AlarmScheduler.cancel(getApplication(), reminder)
            }
        }
    }
}
