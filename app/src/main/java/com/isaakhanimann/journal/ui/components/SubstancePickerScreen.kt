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

package com.isaakhanimann.journal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.di.SubstanceResultHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubstancePickerViewModel @Inject constructor(
    private val substanceRepository: SubstanceRepository,
    private val resultHolder: SubstanceResultHolder,
    experienceRepository: ExperienceRepository
) : ViewModel() {

    val customSubstancesFlow: StateFlow<List<CustomSubstance>> = experienceRepository
        .getCustomSubstancesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSubstances: List<String> by lazy {
        val allSubstances = mutableSetOf<String>()
        substanceRepository.getAllSubstances().map { it.name }.forEach { substanceName ->
            allSubstances.add(substanceName)
        }
        allSubstances.sorted()
    }

    fun selectSubstance(substanceName: String) {
        resultHolder.postResult(substanceName)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstancePickerScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubstancePickerViewModel = hiltViewModel()
) {
    val customSubstances by viewModel.customSubstancesFlow.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val filteredSubstances = remember(searchText, viewModel.allSubstances) {
        if (searchText.isBlank()) {
            viewModel.allSubstances
        } else {
            viewModel.allSubstances.filter { it.contains(searchText, ignoreCase = true) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_substances)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            if (filteredCustomSubstances.isNotEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.custom_substances),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                items(filteredCustomSubstances) { customSubstance ->
                    ListItem(
                        headlineContent = { Text(customSubstance.name) },
                        modifier = Modifier.clickable {
                            viewModel.selectSubstance(customSubstance.name)
                            onNavigateBack()
                        }
                    )
                }
            }

            if (filteredSubstances.isNotEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.substances),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                items(filteredSubstances) { substanceName ->
                    ListItem(
                        headlineContent = { Text(substanceName) },
                        modifier = Modifier.clickable {
                            viewModel.selectSubstance(substanceName)
                            onNavigateBack()
                        }
                    )
                }
            }
        }
    }
}
