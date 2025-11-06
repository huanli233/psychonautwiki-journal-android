/*
 * Copyright (c) 2025. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance

/**
 * Reusable substance picker component
 * Can be used in dialogs, screens, or as part of a larger form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstancePickerDialog(
    onDismiss: () -> Unit,
    onSubstanceSelected: (SubstanceSelection) -> Unit,
    psychonautWikiSubstances: List<String>,
    customSubstances: List<CustomSubstance>,
    currentSelection: SubstanceSelection? = null
) {
    var searchText by remember { mutableStateOf("") }
    
    val filteredPsychonautWikiSubstances = remember(searchText, psychonautWikiSubstances) {
        if (searchText.isBlank()) {
            psychonautWikiSubstances
        } else {
            psychonautWikiSubstances.filter { 
                it.contains(searchText, ignoreCase = true) 
            }
        }
    }
    
    val filteredCustomSubstances = remember(searchText, customSubstances) {
        if (searchText.isBlank()) {
            customSubstances
        } else {
            customSubstances.filter { 
                it.name.contains(searchText, ignoreCase = true) 
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(stringResource(R.string.substance_picker)) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_substance)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Substance list
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // Custom substances section
                    if (filteredCustomSubstances.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.custom_substances),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(filteredCustomSubstances) { customSubstance ->
                            SubstanceListItem(
                                name = customSubstance.name,
                                isSelected = currentSelection is SubstanceSelection.Custom && 
                                           currentSelection.customSubstance.id == customSubstance.id,
                                onClick = {
                                    onSubstanceSelected(SubstanceSelection.Custom(customSubstance))
                                    onDismiss()
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // PsychonautWiki substances section
                    if (filteredPsychonautWikiSubstances.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.psychonautwiki_substance),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(filteredPsychonautWikiSubstances) { substanceName ->
                            SubstanceListItem(
                                name = substanceName,
                                isSelected = currentSelection is SubstanceSelection.PsychonautWiki && 
                                           currentSelection.substanceName == substanceName,
                                onClick = {
                                    onSubstanceSelected(SubstanceSelection.PsychonautWiki(substanceName))
                                    onDismiss()
                                }
                            )
                        }
                    }
                    
                    // No results
                    if (filteredCustomSubstances.isEmpty() && filteredPsychonautWikiSubstances.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_matching_substance_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SubstanceListItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

/**
 * Sealed class to represent substance selection
 */
sealed class SubstanceSelection {
    data class PsychonautWiki(val substanceName: String) : SubstanceSelection()
    data class Custom(val customSubstance: CustomSubstance) : SubstanceSelection()
    
    fun getName(): String = when (this) {
        is PsychonautWiki -> substanceName
        is Custom -> customSubstance.name
    }
}

/**
 * Compact substance picker button that opens a dialog
 */
@Composable
fun SubstancePickerButton(
    selectedSubstance: SubstanceSelection?,
    onSubstanceSelected: (SubstanceSelection) -> Unit,
    psychonautWikiSubstances: List<String>,
    customSubstances: List<CustomSubstance>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(
            text = selectedSubstance?.getName() 
                ?: stringResource(R.string.select_substance),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    
    if (showDialog) {
        SubstancePickerDialog(
            onDismiss = { showDialog = false },
            onSubstanceSelected = onSubstanceSelected,
            psychonautWikiSubstances = psychonautWikiSubstances,
            customSubstances = customSubstances,
            currentSelection = selectedSubstance
        )
    }
}
