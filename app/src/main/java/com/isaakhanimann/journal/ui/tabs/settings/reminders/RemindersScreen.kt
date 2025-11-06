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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersScreenContent(
    reminders: List<IngestionReminder>,
    onAddReminder: () -> Unit,
    onEditReminder: (IngestionReminder) -> Unit,
    onDeleteReminder: (IngestionReminder) -> Unit,
    onToggleReminder: (IngestionReminder, Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<IngestionReminder?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ingestion_reminders)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReminder,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_reminder)) }
            )
        }
    ) { paddingValues ->
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.no_reminders_yet),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.no_reminders_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onEdit = { onEditReminder(reminder) },
                        onDelete = { showDeleteDialog = reminder },
                        onToggle = { enabled -> onToggleReminder(reminder, enabled) }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { reminder ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_reminder)) },
            text = { Text(stringResource(R.string.delete_reminder_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteReminder(reminder)
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: IngestionReminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.substanceName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = reminder.reminderTime.format(timeFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (reminder.dose != null && reminder.units != null) {
                        Text(
                            text = "${reminder.dose} ${reminder.units}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (reminder.note.isNotEmpty()) {
                        Text(
                            text = reminder.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Text(
                        text = when (reminder.repeatMode) {
                            com.isaakhanimann.journal.data.room.experiences.entities.RepeatMode.DAILY -> 
                                stringResource(R.string.reminder_repeat_daily)
                            com.isaakhanimann.journal.data.room.experiences.entities.RepeatMode.WEEKLY -> 
                                stringResource(R.string.reminder_repeat_weekly)
                            com.isaakhanimann.journal.data.room.experiences.entities.RepeatMode.CUSTOM -> 
                                stringResource(R.string.reminder_repeat_custom)
                            else -> stringResource(R.string.reminder_repeat_daily)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggle
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_reminder)
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_reminder)
                    )
                }
            }
        }
    }
}


@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
    navigateToAddReminder: () -> Unit,
    navigateToEditReminder: (Int) -> Unit,
    navigateBack: () -> Unit,
) {
    val reminders by viewModel.reminders.collectAsState()
    
    RemindersScreenContent(
        reminders = reminders,
        onAddReminder = navigateToAddReminder,
        onEditReminder = { reminder -> navigateToEditReminder(reminder.id) },
        onDeleteReminder = viewModel::deleteReminder,
        onToggleReminder = viewModel::toggleReminder,
        navigateBack = navigateBack
    )
}