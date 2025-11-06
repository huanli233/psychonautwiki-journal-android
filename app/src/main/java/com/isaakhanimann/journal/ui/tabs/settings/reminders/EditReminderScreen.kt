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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminderId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToSubstancePicker: () -> Unit,
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val substanceName by viewModel.substanceName.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val customRepeatData by viewModel.customRepeatData.collectAsState()
    val dose by viewModel.dose.collectAsState()
    val units by viewModel.units.collectAsState()
    val note by viewModel.note.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val notificationPermissionState = rememberNotificationPermissionState()

    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Load reminder on first composition
    LaunchedEffect(reminderId) {
        viewModel.loadReminder(reminderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_reminder)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (notificationPermissionState.hasPermission) {
                        viewModel.updateReminder(
                            onSuccess = onNavigateBack,
                            onError = { error -> showErrorDialog = error }
                        )
                    } else {
                        showPermissionDialog = true
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(R.string.update_reminder)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AddOrEditReminderScreenContent(
                substanceName = substanceName,
                onSubstanceNameChange = viewModel::updateSubstanceName,
                reminderTime = reminderTime,
                onReminderTimeChange = viewModel::updateReminderTime,
                repeatMode = repeatMode,
                onRepeatModeChange = viewModel::updateRepeatMode,
                customRepeatData = customRepeatData,
                onCustomRepeatDataChange = viewModel::updateCustomRepeatData,
                dose = dose,
                onDoseChange = viewModel::updateDose,
                units = units,
                onUnitsChange = viewModel::updateUnits,
                note = note,
                onNoteChange = viewModel::updateNote,
                isEnabled = isEnabled,
                onIsEnabledChange = viewModel::updateIsEnabled,
                onNavigateToSubstancePicker = onNavigateToSubstancePicker,
                isEditMode = true,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Error dialog
    showErrorDialog?.let { error ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notification Permission Required") },
            text = { 
                Text("This app needs notification permission to send reminder notifications. Please grant the permission in settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        if (notificationPermissionState.shouldShowRationale) {
                            notificationPermissionState.requestPermission()
                        } else {
                            notificationPermissionState.openSettings()
                        }
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Loading indicator
    if (isSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.saving)) },
            text = {
                CircularProgressIndicator()
            },
            confirmButton = { }
        )
    }
}
