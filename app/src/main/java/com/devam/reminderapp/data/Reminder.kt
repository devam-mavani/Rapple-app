package com.devam.reminderapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val triggerAtMillis: Long,   // when the reminder should fire
    val isCompleted: Boolean = false
)
