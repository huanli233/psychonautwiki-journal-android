package com.isaakhanimann.journal.ui.tabs.settings.customrecipes

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
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.di.RecipeResultHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class SubstanceOrUnit {
    data class Substance(val name: String) : SubstanceOrUnit()
    data class Unit(val customUnit: CustomUnit) : SubstanceOrUnit()
}

@HiltViewModel
class SubstanceSelectorViewModel @Inject constructor(
    private val resultHolder: RecipeResultHolder,
    experienceRepository: ExperienceRepository
) : ViewModel() {

    val customUnitsFlow: StateFlow<List<CustomUnit>> = experienceRepository
        .getCustomUnitsFlow(isArchived = false)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectSubstance(index: Int, name: String) {
        resultHolder.postResult(index, name, null)
    }

    fun selectCustomUnit(index: Int, customUnit: CustomUnit) {
        resultHolder.postResult(index, customUnit.substanceName, customUnit.id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceSelectorScreen(
    subcomponentIndex: Int,
    allSubstances: List<String>,
    onSubstanceSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: SubstanceSelectorViewModel = hiltViewModel()
    val customUnits by viewModel.customUnitsFlow.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val filteredSubstances = remember(searchText, allSubstances) {
        if (searchText.isBlank()) {
            allSubstances
        } else {
            allSubstances.filter { it.contains(searchText, ignoreCase = true) }
        }
    }

    val filteredCustomUnits = remember(searchText, customUnits) {
        if (searchText.isBlank()) {
            customUnits
        } else {
            customUnits.filter { unit ->
                unit.name.contains(searchText, ignoreCase = true) ||
                        unit.substanceName.contains(searchText, ignoreCase = true) ||
                        unit.note.contains(searchText, ignoreCase = true)
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
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            if (filteredSubstances.isNotEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Substances",
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
                            viewModel.selectSubstance(subcomponentIndex, substanceName)
                            onSubstanceSelected()
                        }
                    )
                }
            }

            if (filteredCustomUnits.isNotEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Custom Units",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                items(filteredCustomUnits) { customUnit ->
                    ListItem(
                        headlineContent = { Text(customUnit.name) },
                        supportingContent = {
                            Text("${customUnit.substanceName} - ${customUnit.getDoseOfOneUnitDescription()}")
                        },
                        modifier = Modifier.clickable {
                            viewModel.selectCustomUnit(subcomponentIndex, customUnit)
                            onSubstanceSelected()
                        }
                    )
                }
            }
        }
    }
}