package com.devam.reminderapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devam.reminderapp.data.Reminder
import com.devam.reminderapp.ui.ReminderViewModel
import com.devam.reminderapp.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val viewModel: ReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReminderScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reminders") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            PermissionBanner(context)

            if (reminders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reminders yet. Tap + to add one.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderRow(
                            reminder = reminder,
                            onToggle = { viewModel.toggleCompleted(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, note, millis ->
                viewModel.addReminder(title, note, millis)
                showAddDialog = false
            }
        )
    }
}

/** Simple banner nudging the user to grant overlay + exact-alarm + notification permissions. */
@Composable
fun PermissionBanner(context: Context) {
    val needsOverlay = !Settings.canDrawOverlays(context)
    val needsExactAlarm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        !(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    if (needsOverlay || needsExactAlarm) {
        Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Setup needed for reminders to pop up over other apps:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (needsOverlay) {
                    Button(onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }) { Text("Allow display over other apps") }
                    Spacer(Modifier.height(8.dp))
                }
                if (needsExactAlarm) {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }) { Text("Allow exact alarms") }
                }
            }
        }
    }
}

@Composable
fun ReminderRow(reminder: Reminder, onToggle: () -> Unit, onDelete: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEE, MMM d Ā· h:mm a", Locale.getDefault()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = reminder.isCompleted, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(reminder.title, fontWeight = FontWeight.Medium)
            if (reminder.note.isNotBlank()) Text(reminder.note, style = MaterialTheme.typography.bodySmall)
            Text(
                formatter.format(Date(reminder.triggerAtMillis)),
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onSave: (String, String, Long) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.MINUTE, 5) } }
    var pickedLabel by remember {
        mutableStateOf(SimpleDateFormat("EEE, MMM d Ā· h:mm a", Locale.getDefault()).format(calendar.time))
    }

    fun refreshLabel() {
        pickedLabel = SimpleDateFormat("EEE, MMM d Ā· h:mm a", Locale.getDefault()).format(calendar.time)
    }

    fun pickDateThenTime() {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(Calendar.YEAR, y); calendar.set(Calendar.MONTH, m); calendar.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min); calendar.set(Calendar.SECOND, 0)
                        refreshLabel()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                ).show()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New reminder") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") })
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { pickDateThenTime() }) { Text(pickedLabel) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onSave(title, note, calendar.timeInMillis) },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
