package com.devam.reminderapp.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {

    val allReminders: Flow<List<Reminder>> = dao.getAll()

    suspend fun add(reminder: Reminder): Long = dao.insert(reminder)

    suspend fun update(reminder: Reminder) = dao.update(reminder)

    suspend fun delete(reminder: Reminder) = dao.delete(reminder)

    suspend fun getById(id: Long): Reminder? = dao.getById(id)
}
