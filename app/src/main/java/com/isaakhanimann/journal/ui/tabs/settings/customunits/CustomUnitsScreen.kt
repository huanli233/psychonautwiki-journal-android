package com.isaakhanimann.journal.ui.tabs.settings.customunits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer

@Composable
fun CustomUnitsScreen(
    viewModel: CustomUnitsViewModel = hiltViewModel(),
    navigateToEditCustomUnit: (customUnitId: Int) -> Unit,
    navigateToAddCustomUnit: () -> Unit,
    navigateToCustomUnitArchive: () -> Unit,
) {
    CustomUnitsScreenContent(
        filteredUnits = viewModel.filteredCustomUnitsFlow.collectAsState().value,
        navigateToEditCustomUnit = navigateToEditCustomUnit,
        navigateToAddCustomUnit = navigateToAddCustomUnit,
        navigateToCustomUnitArchive = navigateToCustomUnitArchive,
        searchText = viewModel.searchTextFlow.collectAsState().value,
        onSearch = viewModel::onSearch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomUnitsScreenContent(
    filteredUnits: List<CustomUnit>,
    navigateToEditCustomUnit: (customUnitId: Int) -> Unit,
    navigateToAddCustomUnit: () -> Unit,
    navigateToCustomUnitArchive: () -> Unit,
    searchText: String,
    onSearch: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_units)) },
                actions = {
                    IconButton(onClick = navigateToCustomUnitArchive) {
                        Icon(Icons.Default.Inventory, contentDescription = stringResource(R.string.archive_title))
                    }
                })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.imePadding(),
                onClick = navigateToAddCustomUnit,
                icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_custom_unit)) },
                text = { Text(text = stringResource(R.string.add)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                value = searchText,
                onValueChange = onSearch,
                shape = CircleShape,
                placeholder = { Text(text = stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onSearch("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_search)
                            )
                        }
                    }
                },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
                singleLine = true,
            )

            if (filteredUnits.isEmpty()) {
                val title = if (searchText.isEmpty()) stringResource(R.string.no_custom_units_yet) else stringResource(R.string.no_custom_units_found)
                val description = if (searchText.isEmpty()) stringResource(R.string.no_custom_units_description) else stringResource(R.string.no_custom_units_found_description)
                EmptyScreenDisclaimer(title = title, description = description)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredUnits, key = { it.id }) { customUnit ->
                        CustomUnitRow(
                            customUnit = customUnit,
                            navigateToEditCustomUnit = navigateToEditCustomUnit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomUnitRow(
    customUnit: CustomUnit,
    navigateToEditCustomUnit: (customUnitId: Int) -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = { navigateToEditCustomUnit(customUnit.id) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            androidx.compose.material3.Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${customUnit.substanceName} • ${customUnit.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Text(
                    text = "${customUnit.getDoseOfOneUnitDescription()} per ${customUnit.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (customUnit.note.isNotBlank()) {
                    Text(
                        text = customUnit.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomUnitsScreenPreview() {
    CustomUnitsScreenContent(
        filteredUnits = listOf(CustomUnit.mdmaSample, CustomUnit.twoCBSample),
        navigateToEditCustomUnit = {},
        navigateToAddCustomUnit = {},
        navigateToCustomUnitArchive = {},
        searchText = "",
        onSearch = {}
    )
}