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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * BroadcastReceiver that handles actions from reminder notifications
 * (Mark as taken, Snooze, Skip)
 */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: IngestionReminderNotificationManager
    
    @Inject
    lateinit var experienceRepository: ExperienceRepository

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        if (reminderId == -1) return

        when (intent.action) {
            "ACTION_MARK_TAKEN" -> {
                val substanceName = intent.getStringExtra("substance_name") ?: ""
                val dose = intent.getDoubleExtra("dose", 0.0).takeIf { it != 0.0 }
                val units = intent.getStringExtra("units")
                val note = intent.getStringExtra("note") ?: ""
                
                // Create ingestion entry automatically
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Get or create today's experience
                        val now = Instant.now()
                        val experience = Experience(
                            id = 0,
                            title = "Reminder: $substanceName",
                            text = note,
                            sortDate = now,
                            location = null
                        )
                        
                        val ingestion = Ingestion(
                            substanceName = substanceName,
                            time = now,
                            administrationRoute = AdministrationRoute.ORAL,
                            dose = dose,
                            isDoseAnEstimate = false,
                            estimatedDoseStandardDeviation = null,
                            units = units,
                            experienceId = 0, // Will be set by repository
                            notes = "From reminder",
                            stomachFullness = null,
                            consumerName = null,
                            customUnitId = null
                        )
                        
                        val substanceCompanion = SubstanceCompanion(
                            substanceName = substanceName,
                            color = null
                        )
                        
                        experienceRepository.insertIngestionExperienceAndCompanion(
                            ingestion,
                            experience,
                            substanceCompanion
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                Toast.makeText(
                    context,
                    context.getString(R.string.reminder_marked_taken),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Dismiss the notification
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                notifManager.cancel(IngestionReminderNotificationManager.NOTIFICATION_ID_BASE + reminderId)
            }
            
            "ACTION_SNOOZE" -> {
                val substanceName = intent.getStringExtra("substance_name") ?: ""
                val dose = intent.getDoubleExtra("dose", 0.0).takeIf { it != 0.0 }
                val units = intent.getStringExtra("units")
                val note = intent.getStringExtra("note") ?: ""
                
                notificationManager.snoozeReminder(reminderId, substanceName, dose, units, note)
                
                Toast.makeText(
                    context,
                    context.getString(R.string.reminder_snoozed),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Dismiss current notification
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                notifManager.cancel(IngestionReminderNotificationManager.NOTIFICATION_ID_BASE + reminderId)
            }
            
            "ACTION_SKIP" -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.reminder_skipped),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Dismiss the notification
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                notifManager.cancel(IngestionReminderNotificationManager.NOTIFICATION_ID_BASE + reminderId)
            }
        }
    }
}
