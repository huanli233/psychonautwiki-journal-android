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

package com.isaakhanimann.journal.ui.tabs.journal.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSubstanceSearchBar(
    selectedSubstances: List<String>,
    availableSubstances: List<String>,
    onAddSubstance: (String) -> Unit,
    onRemoveSubstance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubstanceSelector by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Display selected substances as chips
        if (selectedSubstances.isNotEmpty()) {
            Text(
                text = stringResource(R.string.searching_for_substances, selectedSubstances.joinToString(", ")),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedSubstances.forEach { substance ->
                    FilterChip(
                        selected = true,
                        onClick = { onRemoveSubstance(substance) },
                        label = { Text(substance) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_substance_from_search),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
        
        // Button to add more substances
        TextButton(
            onClick = { showSubstanceSelector = !showSubstanceSelector },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (selectedSubstances.isEmpty()) 
                    stringResource(R.string.search_multiple_substances)
                else 
                    stringResource(R.string.add_substance_to_search)
            )
        }
        
        // Substance selector dialog
        AnimatedVisibility(visible = showSubstanceSelector) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 300.dp)
            ) {
                LazyColumn {
                    items(
                        availableSubstances.filter { !selectedSubstances.contains(it) }
                    ) { substance ->
                        ListItem(
                            headlineContent = { Text(substance) },
                            modifier = Modifier.clickable {
                                onAddSubstance(substance)
                                showSubstanceSelector = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
