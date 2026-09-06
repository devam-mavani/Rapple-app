# Reminders (Pixel)

A minimal reminder app: add a reminder with a title/note/time, and at that time
it pops up a floating card **over whatever app you're using** (like Google's
reminder overlay), plus a normal notification as a backup.

## How to open it
1. Install **Android Studio** (Koala or newer).
2. `File -> Open` and select this `ReminderApp` folder. Studio will generate
   the Gradle wrapper automatically on first sync (no need to run `gradle`
   yourself).
3. Let Gradle sync, then hit Run with your Pixel connected (USB debugging on)
   or an emulator.

## First-run permissions (the app will prompt for these itself)
- **Display over other apps** — required for the floating popup. Without it,
  you'll still get a normal notification, just no overlay.
- **Alarms & reminders (exact alarms)** — required on Android 12+ so the
  reminder fires at the exact minute instead of being delayed by the system.
- **Notifications** — Android 13+ will ask at first launch.

## How it's built
- `data/` — Room database (`Reminder` entity, DAO, repository) storing your
  reminders locally on-device.
- `ui/` — Jetpack Compose screen (list + "add reminder" dialog with a
  date/time picker), and the ViewModel wiring it to the database.
- `util/AlarmScheduler.kt` — schedules an exact `AlarmManager` alarm for each
  reminder's time.
- `service/ReminderAlarmReceiver.kt` — fires when the alarm goes off: posts a
  notification and (if permission is granted) starts the overlay.
- `service/OverlayService.kt` — the actual floating window, drawn using
  `WindowManager` with `TYPE_APPLICATION_OVERLAY`, with Snooze/Dismiss buttons.
- `service/BootReceiver.kt` — re-schedules all pending reminders after a phone
  restart (alarms don't survive a reboot otherwise).

## Getting an APK without installing Android Studio

This project includes a GitHub Actions workflow (`.github/workflows/build-apk.yml`)
that builds a debug APK in the cloud every time you push to `main`.

1. Create a new **public or private repo** on GitHub (e.g. `reminder-app`).
2. Push this entire `ReminderApp` folder to it:
   ```
   cd ReminderApp
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/reminder-app.git
   git push -u origin main
   ```
3. On GitHub, open the **Actions** tab of your repo — a "Build Debug APK" run
   will already be in progress (or trigger it manually with the "Run workflow"
   button).
4. Once it finishes (green check), open the run and scroll to **Artifacts** —
   download `reminder-app-debug-apk` (a zip containing `app-debug.apk`).
5. On your phone: unzip if needed, tap the `.apk` file to install. You'll need
   to allow "install unknown apps" for your browser/file manager the first
   time Android asks.

This APK is **debug-signed** (fine for installing on your own device, not for
the Play Store). If you ever want a release build, that needs a signing key —
ask and I can add that step too.

## Things worth adding next
- Real "snooze" logic (currently Snooze just closes the popup — hook it up to
  re-schedule +10 min).
- Recurring reminders (daily/weekly).
- Swipe-to-delete and editing an existing reminder.
- A nicer overlay design (rounded corners, app icon, swipe-to-dismiss).
