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
 * along with PsychonautWiki Journal. If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.settings.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRepeatData

@Composable
fun CustomRepeatDialog(
    initialData: CustomRepeatData?,
    onDismiss: () -> Unit,
    onConfirm: (CustomRepeatData) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(initialData?.daysOfWeek ?: emptySet()) }
    var intervalDays by remember { mutableStateOf(initialData?.intervalDays?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Custom Repeat Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedTab = 0 },
                        label = { Text("Days of Week") },
                        selected = selectedTab == 0,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { selectedTab = 1 },
                        label = { Text("Interval") },
                        selected = selectedTab == 1,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                when (selectedTab) {
                    0 -> {
                        // Days of week selection
                        Text(
                            text = "Select days of the week:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dayNames.forEachIndexed { index, dayName ->
                                val dayNumber = index + 1
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .toggleable(
                                            value = selectedDays.contains(dayNumber),
                                            onValueChange = { isSelected ->
                                                selectedDays = if (isSelected) {
                                                    selectedDays + dayNumber
                                                } else {
                                                    selectedDays - dayNumber
                                                }
                                            }
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedDays.contains(dayNumber),
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Interval days
                        Text(
                            text = "Repeat every X days:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = intervalDays,
                            onValueChange = { intervalDays = it },
                            label = { Text("Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 2 for every 2 days") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val customData = when (selectedTab) {
                        0 -> CustomRepeatData(daysOfWeek = selectedDays)
                        1 -> {
                            val interval = intervalDays.toIntOrNull()
                            if (interval != null && interval > 0) {
                                CustomRepeatData(intervalDays = interval)
                            } else {
                                CustomRepeatData()
                            }
                        }
                        else -> CustomRepeatData()
                    }
                    onConfirm(customData)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
