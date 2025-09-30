package com.isaakhanimann.journal.ui.tabs.settings.customrecipes.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.substances.AdministrationRoute

@Composable
fun EditCustomRecipeScreen(
    viewModel: EditCustomRecipeViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToSubstanceSelector: (Int) -> Unit
) {
    val allSubstances by viewModel.allSubstances.collectAsState()

    EditCustomRecipeScreenContent(
        recipeName = viewModel.recipeName,
        onRecipeNameChange = viewModel::updateRecipeName,
        administrationRoute = viewModel.administrationRoute,
        onAdministrationRouteChange = viewModel::updateAdministrationRoute,
        unit = viewModel.unit,
        onUnitChange = viewModel::updateUnit,
        unitPlural = viewModel.unitPlural,
        onUnitPluralChange = viewModel::updateUnitPlural,
        note = viewModel.note,
        onNoteChange = viewModel::updateNote,
        subcomponents = viewModel.subcomponents,
        onAddSubcomponent = viewModel::addSubcomponent,
        onRemoveSubcomponent = viewModel::removeSubcomponent,
        onUpdateSubcomponent = viewModel::updateSubcomponent,
        onSave = { viewModel.saveRecipe(navigateBack) },
        onDelete = { viewModel.deleteRecipe(navigateBack) },
        onToggleArchive = viewModel::toggleArchive,
        isArchived = viewModel.isArchived,
        isValid = viewModel.isValid(),
        isLoading = viewModel.isLoading,
        isDeleting = viewModel.isDeleting,
        allSubstances = allSubstances,
        onSubstanceNameClick = navigateToSubstanceSelector
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomRecipeScreenContent(
    recipeName: String,
    onRecipeNameChange: (String) -> Unit,
    administrationRoute: AdministrationRoute,
    onAdministrationRouteChange: (AdministrationRoute) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    unitPlural: String,
    onUnitPluralChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    subcomponents: List<EditCustomRecipeViewModel.SubcomponentData>,
    onAddSubcomponent: () -> Unit,
    onRemoveSubcomponent: (Int) -> Unit,
    onUpdateSubcomponent: (Int, EditCustomRecipeViewModel.SubcomponentData) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onToggleArchive: () -> Unit,
    isArchived: Boolean,
    isValid: Boolean,
    isLoading: Boolean,
    isDeleting: Boolean,
    allSubstances: List<String>,
    onSubstanceNameClick: (Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var isRouteMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_custom_recipe)) },
                actions = {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isArchived) "Unarchive" else "Archive") },
                            onClick = {
                                onToggleArchive()
                                showDropdownMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_recipe)) },
                            onClick = {
                                showDeleteDialog = true
                                showDropdownMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = onRecipeNameChange,
                    label = { Text(stringResource(R.string.recipe_name)) },
                    placeholder = { Text(stringResource(R.string.recipe_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = isRouteMenuExpanded,
                    onExpandedChange = { isRouteMenuExpanded = !isRouteMenuExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        value = administrationRoute.displayText,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.administration_route)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRouteMenuExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = isRouteMenuExpanded,
                        onDismissRequest = { isRouteMenuExpanded = false },
                    ) {
                        AdministrationRoute.values().forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.displayText) },
                                onClick = {
                                    onAdministrationRouteChange(selectionOption)
                                    isRouteMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text(stringResource(R.string.recipe_unit)) },
                        placeholder = { Text(stringResource(R.string.recipe_unit_hint)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitPlural,
                        onValueChange = onUnitPluralChange,
                        label = { Text(stringResource(R.string.recipe_unit_plural)) },
                        placeholder = { Text(stringResource(R.string.recipe_unit_plural_hint)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(stringResource(R.string.recipe_note)) },
                    placeholder = { Text(stringResource(R.string.recipe_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.subcomponents),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(onClick = onAddSubcomponent) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_subcomponent))
                    }
                }
            }

            itemsIndexed(subcomponents) { index, subcomponent ->
                SubcomponentCard(
                    unit = unit,
                    subcomponent = subcomponent,
                    onUpdate = { updatedSubcomponent ->
                        onUpdateSubcomponent(index, updatedSubcomponent)
                    },
                    onRemove = { onRemoveSubcomponent(index) },
                    onSubstanceNameClick = { onSubstanceNameClick(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSave,
                    enabled = isValid && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_recipe))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_recipe)) },
            text = { Text(stringResource(R.string.delete_recipe_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    enabled = !isDeleting
                ) {
                    Text(stringResource(R.string.delete_recipe))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SubcomponentCard(
    unit: String,
    subcomponent: EditCustomRecipeViewModel.SubcomponentData,
    onUpdate: (EditCustomRecipeViewModel.SubcomponentData) -> Unit,
    onRemove: () -> Unit,
    onSubstanceNameClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.subcomponents),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_subcomponent))
                }
            }

            OutlinedTextField(
                value = subcomponent.substanceName,
                onValueChange = { /* No-op */ },
                label = { Text(stringResource(R.string.substance_name)) },
                placeholder = { Text(stringResource(R.string.substance_name_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSubstanceNameClick),
                readOnly = true,
                enabled = false, // To prevent focus and keyboard
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = subcomponent.dose,
                    onValueChange = { onUpdate(subcomponent.copy(dose = it)) },
                    label = { Text(stringResource(R.string.dose_per_unit, unit)) },
                    placeholder = { Text(stringResource(R.string.dose_per_unit_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = subcomponent.originalUnit,
                    onValueChange = { onUpdate(subcomponent.copy(originalUnit = it)) },
                    label = { Text(stringResource(R.string.original_unit)) },
                    placeholder = { Text(stringResource(R.string.original_unit_hint)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = subcomponent.isEstimate,
                    onCheckedChange = { onUpdate(subcomponent.copy(isEstimate = it)) }
                )
                Text(stringResource(R.string.is_estimate))
            }

            if (subcomponent.isEstimate) {
                OutlinedTextField(
                    value = subcomponent.estimatedDoseStandardDeviation,
                    onValueChange = { onUpdate(subcomponent.copy(estimatedDoseStandardDeviation = it)) },
                    label = { Text(stringResource(R.string.estimated_dose_standard_deviation)) },
                    placeholder = { Text(stringResource(R.string.estimated_dose_standard_deviation_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
fun EditCustomRecipeScreenPreview() {
    EditCustomRecipeScreenContent(
        recipeName = "Mixed Capsule",
        onRecipeNameChange = {},
        administrationRoute = AdministrationRoute.ORAL,
        onAdministrationRouteChange = {},
        unit = "capsule",
        onUnitChange = {},
        unitPlural = "capsules",
        onUnitPluralChange = {},
        note = "Sample note",
        onNoteChange = {},
        subcomponents = listOf(
            EditCustomRecipeViewModel.SubcomponentData(
                id = 1,
                substanceName = "MDMA",
                dose = "80",
                originalUnit = "mg"
            )
        ),
        onAddSubcomponent = {},
        onRemoveSubcomponent = {},
        onUpdateSubcomponent = { _, _ -> },
        onSave = {},
        onDelete = {},
        onToggleArchive = {},
        isArchived = false,
        isValid = true,
        isLoading = false,
        isDeleting = false,
        allSubstances = listOf("MDMA", "LSD", "Caffeine"),
        onSubstanceNameClick = {}
    )
}