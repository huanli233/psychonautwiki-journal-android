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

package com.isaakhanimann.journal.ui.tabs.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder
import com.isaakhanimann.journal.data.room.experiences.entities.RepeatMode
import com.isaakhanimann.journal.notifications.IngestionReminderNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val experienceRepository: ExperienceRepository,
    private val notificationManager: IngestionReminderNotificationManager,
    private val substanceResultHolder: com.isaakhanimann.journal.di.SubstanceResultHolder
) : ViewModel() {

    private val _substanceName = MutableStateFlow("")
    val substanceName: StateFlow<String> = _substanceName.asStateFlow()

    private val _reminderTime = MutableStateFlow(LocalTime.now())
    val reminderTime: StateFlow<LocalTime> = _reminderTime.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.DAILY)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _dose = MutableStateFlow("")
    val dose: StateFlow<String> = _dose.asStateFlow()

    private val _units = MutableStateFlow("")
    val units: StateFlow<String> = _units.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _customRepeatData = MutableStateFlow<com.isaakhanimann.journal.data.room.experiences.entities.CustomRepeatData?>(null)
    val customRepeatData: StateFlow<com.isaakhanimann.journal.data.room.experiences.entities.CustomRepeatData?> = _customRepeatData.asStateFlow()

    private var currentReminder: IngestionReminder? = null

    init {
        // Listen for substance selection results
        viewModelScope.launch {
            substanceResultHolder.resultFlow.collect { selectedSubstance ->
                _substanceName.value = selectedSubstance
            }
        }
    }

    fun loadReminder(reminderId: Int) {
        viewModelScope.launch {
            try {
                val reminder = experienceRepository.getReminderById(reminderId)
                if (reminder != null) {
                    currentReminder = reminder
                    _substanceName.value = reminder.substanceName
                    _reminderTime.value = reminder.reminderTime
                    _repeatMode.value = reminder.repeatMode
                    _dose.value = reminder.dose?.toString() ?: ""
                    _units.value = reminder.units ?: ""
                    _note.value = reminder.note
                    _isEnabled.value = reminder.isEnabled
                    _customRepeatData.value = com.isaakhanimann.journal.data.room.experiences.entities.CustomRepeatData.fromJson(reminder.customRepeatData)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun updateSubstanceName(name: String) {
        _substanceName.value = name
    }

    fun updateReminderTime(time: LocalTime) {
        _reminderTime.value = time
    }

    fun updateRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun updateDose(dose: String) {
        _dose.value = dose
    }

    fun updateUnits(units: String) {
        _units.value = units
    }

    fun updateNote(note: String) {
        _note.value = note
    }

    fun updateIsEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    fun updateCustomRepeatData(data: com.isaakhanimann.journal.data.room.experiences.entities.CustomRepeatData?) {
        _customRepeatData.value = data
    }

    fun updateReminder(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val reminder = currentReminder
        if (reminder == null) {
            onError("Reminder not found")
            return
        }

        if (_substanceName.value.isBlank()) {
            onError("Substance name is required")
            return
        }

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val updatedReminder = reminder.copy(
                    substanceName = _substanceName.value,
                    reminderTime = _reminderTime.value,
                    repeatMode = _repeatMode.value,
                    dose = _dose.value.toDoubleOrNull(),
                    units = _units.value.takeIf { it.isNotBlank() },
                    note = _note.value,
                    isEnabled = _isEnabled.value,
                    customRepeatData = com.isaakhanimann.journal.data.room.experiences.entities.CustomRepeatData.toJson(_customRepeatData.value)
                )

                experienceRepository.update(updatedReminder)
                
                // Cancel existing notification
                notificationManager.cancelReminder(reminder.id)
                
                // Schedule new notification if enabled
                if (_isEnabled.value) {
                    notificationManager.scheduleReminder(updatedReminder)
                }

                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.message ?: "Failed to update reminder")
            }
        }
    }
}
