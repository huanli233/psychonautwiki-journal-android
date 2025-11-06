/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngestionReminderNotificationManager @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val CHANNEL_ID = "ingestion_reminders"
        const val CHANNEL_NAME = "Ingestion Reminders"
        const val NOTIFICATION_ID_BASE = 10000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_name)
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule a reminder notification
     */
    fun scheduleReminder(reminder: IngestionReminder) {
        if (!reminder.isEnabled) return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("substance_name", reminder.substanceName)
            putExtra("dose", reminder.dose)
            putExtra("units", reminder.units)
            putExtra("note", reminder.note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_BASE + reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val reminderDateTime = LocalDateTime.of(today, reminder.reminderTime)
        
        val scheduledTime = if (reminderDateTime.isBefore(now) || reminderDateTime.isEqual(now)) {
            reminderDateTime.plusDays(1)
        } else {
            reminderDateTime
        }
        
        val triggerAtMillis = scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancel a scheduled reminder
     */
    fun cancelReminder(reminderId: Int) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_BASE + reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Show a notification for an ingestion reminder
     */
    fun showReminderNotification(
        reminderId: Int,
        substanceName: String,
        dose: Double?,
        units: String?,
        note: String
    ) {
        val doseText = if (dose != null && units != null) {
            "$dose $units"
        } else {
            ""
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Ingestion Reminder")
            .setContentText("Time to take $substanceName $doseText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$substanceName $doseText${if (note.isNotEmpty()) "\n$note" else ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(createOpenAppIntent())
            .addAction(
                0,
                context.getString(R.string.add_ingestion),
                createAddIngestionIntent(substanceName, dose, units)
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + reminderId, notification)
    }

    /**
     * Test method to show a notification immediately
     */
    fun testNotification() {
        showReminderNotification(
            reminderId = 999,
            substanceName = "Test Substance",
            dose = 10.0,
            units = "mg",
            note = "This is a test notification"
        )
    }

    private fun createOpenAppIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAddIngestionIntent(substanceName: String, dose: Double?, units: String?): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("navigate_to_add_ingestion", true)
            putExtra("substance_name", substanceName)
            putExtra("dose", dose)
            putExtra("units", units)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createMarkAsTakenIntent(reminderId: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = "ACTION_MARK_TAKEN"
            putExtra("reminder_id", reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId * 10 + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozeIntent(reminderId: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("reminder_id", reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId * 10 + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Snooze a reminder for 15 minutes
     */
    fun snoozeReminder(reminderId: Int, substanceName: String, dose: Double?, units: String?, note: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("substance_name", substanceName)
            putExtra("dose", dose)
            putExtra("units", units)
            putExtra("note", note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_BASE + reminderId + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + 15 * 60 * 1000 // 15 minutes

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        }
    }
}
